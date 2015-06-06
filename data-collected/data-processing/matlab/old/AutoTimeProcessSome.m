clear;
close all;


%% PARAMETERS
dbDirectory = 'db';
dbFiles = dir(strcat(dbDirectory, '/db*.db'));
dbFilePaths = {}

type = 'bar'; %type = 'bar' or 'point'
logYaxis = 0; %display log scale? (1 = true)
aggregatedTime = 60; %1 = no aggregation
visible = 'off';

packagesToPlot = {'com.facebook.katana', 'com.facebook.orca', 'com.google.android.gm', 'com.whatsapp','com.google.android.gms','com.google.android.googlequicksearchbox','com.google.android.apps.maps','com.kitkatandroid.keyboard','com.zoodles.kidmode','com.google.android.apps.plus'};


%% COMPUTATIONS
dataX = {};
dataY = {};

databases = ones(1,numel(dbFiles));
for dbIdx = 1 : numel(dbFiles)
    dbPath = strcat(dbDirectory, '/', dbFiles(dbIdx).name);
    dbFilePaths{dbIdx} = dbPath;
    databases(dbIdx) = sqlite3.open(dbPath);
end

for dbIdx = 1 : numel(dbFiles)
    dbPath = strcat(dbDirectory, '/', dbFiles(dbIdx).name);
    [~,dbName,~] = fileparts(dbPath);
    
    database = databases(dbIdx);
    packagesNames = allPackageName(database);
    
    
    for packageIdx = 1:numel(packagesNames)
        package = packagesNames{packageIdx};
        
        results = sqlite3.execute(database, strcat('SELECT * from table_applications_activity WHERE package_name =''', package, ''' AND was_foreground=1 order by record_time'));
        
        display(strcat({'processing ' }, dbName, {': '}, num2str(packageIdx),'/',num2str(numel(packagesNames)), {' packages processed'}));
        
        if(numel(results) > 0)
            %"prealloc"
            dataX{dbIdx,numel(results),packageIdx} = [];
            dataY{dbIdx,numel(results),packageIdx} = [];
            
            lastFT = 0;
            %process data, store in in cell
            for(recordIdx = 1:numel(results))
                rec = results(recordIdx);
                timestamp = (results(recordIdx).record_time+2*60*60000)/1000;
                time = [1970 1 1 0 0 timestamp];
                t = datetime(time,'InputFormat','dd-MMM-yyyy HH:mm:ss');
                t.Second = 0; %round time as record may have + 1 to 10 seconds of delay
                time = datenum(t);
                
                
                dataX{dbIdx, recordIdx,packageIdx} = time;
                ft = rec.foreground_time_usage /1000 / 60; %in minutes
                if(ft < lastFT)
                    lastFT = 0;
                end
                dataY{dbIdx, recordIdx,packageIdx} = ft - lastFT;
                lastFT = ft;
            end
        else
            dataY{dbIdx, 1,packageIdx} = []; % need to add, otherwise package ignored. Cause problem later
            dataX{dbIdx, 1,packageIdx} = [];
        end
        
    end
    
end

%% PLOT

%remove variables no used to avoid problem with indices
clearvars -except databases aggregatedTime dataX dataY logYaxis visible type dbFilePaths packagesToPlot


minYaxis = datenum(seconds(0));
aggregatedTimeDN = datenum(datetime([0,1,0,0,aggregatedTime,0]));


for(dbIdx = 1:numel(dbFilePaths))
    dbPath = dbFilePaths{dbIdx};
    [~,dbName,~] = fileparts(dbPath);
    
    database = databases(dbIdx);
    packagesNames = allPackageName(database);
    
    for(packageIdx = 1:numel(packagesNames))
        package = packagesNames{packageIdx};
        
        
        close all;
        if(strcmp(type, 'bar'))
            
            %process graph only if there are more than 4 records and the
            %total foreground time > 0
            r = strfind(packagesToPlot,package);
            
            if(numel([dataX{dbIdx,:,packageIdx}]) > 0 && sum([dataY{dbIdx,:,packageIdx}]) > 0 && sum([r{:}]) > 0)
                % PLOT BAR
                titleFontSize = 28;
                axisLabelFontSize = 24;
                

                
                %if(numel([dataX_back(:)', dataX_fore(:)', dataX_ibtw(:)']) > 0)
                
                mkdir('D-ft')
                figureNameFT = strcat('D-ft/',package,'-',dbName,'-aggr',num2str(aggregatedTime), '-bar-ft.pdf');
                % figureNameFB = strcat('figures/',packageName,'-',dbName,'-aggr',num2str(aggregatedTime), '-bar-bt.pdf');
                fig_FT = figure('units','normalized','outerposition',[0 0 1 1],'visible',visible);
                
                dataX_plot = [dataX{dbIdx,:,packageIdx}];
                dataY_plot = datenum(minutes([dataY{dbIdx,:,packageIdx}]));
                

                
                if(aggregatedTime > 1)
                    firstDayDN = min(dataX_plot);
                    if(numel(firstDayDN) == 0)
                        firstDayDN = 0; %in case all dataset are empty
                    end
                    firstDayDT = datetime(firstDayDN,'ConvertFrom', 'datenum');
                    firstDayDT.Second = 0;
                    firstDayDT.Minute = 0;
                    firstDayDT.Hour = 0;
                    firstDayDN = datenum(firstDayDT);
                    
                    
                    lastDayDN = max(dataX_plot);
                    lastDayDT = datetime(lastDayDN,'ConvertFrom', 'datenum');
                    lastDayDT.Second = 0;
                    lastDayDT.Minute = 0;
                    lastDayDT.Hour = 0;
                    lastDayDT.Day = lastDayDT.Day + 1;
                    lastDayDN = datenum(lastDayDT);
                    
                    %transform aggregatedTime into datenum
                    
                    [dataX_plot, dataY_plot] = aggregate(dataX_plot, dataY_plot, aggregatedTimeDN, firstDayDN);                 
                end
                
                maxYaxis = max(dataY_plot) + datenum(seconds(1));
                
                resultName = sqlite3.execute(database, strcat('SELECT app_name from table_installed_apps WHERE package_name =''', package,''''));
                appname = resultName(1).app_name;
                
                bar(dataX_plot, dataY_plot) %plotBar(dataX_back, dataY_back, dataX_fore, dataY_fore, dataX_ibtw, dataY_ibtw, showParam, logYaxis, 'off');
                title(strcat(appname, {' - Time usage for '},dbName, {'  - aggregation by '}, num2str(aggregatedTime), {' min'}),'FontSize',titleFontSize);
                
                ylabel('Time used [hh:mm(:ss)]','FontSize',axisLabelFontSize);
                set(gca, 'FontSize', axisLabelFontSize);
                grid on;
                
                ylim([minYaxis, maxYaxis]);
                dynamicDateTicksY();
                dynamicDateTicks();
                
                %datetick('y','MM:SS');
                axx=gca;
                axx.XTickLabelRotation = 45;
                %dynamicDateTicks(axH, 'link', 'dd/mm')
                saveTightFigure(fig_FT, figureNameFT);
                display(strcat({'plotting ' }, dbName, {': '}, num2str(packageIdx),'/',num2str(numel(packagesNames)), {' packages processed'}));
              
            end
            
        else
            % PLOT POINTS
            r = strfind(packagesToPlot,package);
            if(numel([dataX{dbIdx,:,packageIdx}]) > 0 && sum([dataY{dbIdx,:,packageIdx}]) > 0 && sum([r{:}]) > 0 )
                % PLOT BAR
                titleFontSize = 28;
                axisLabelFontSize = 24;
                
                %if(numel([dataX_back(:)', dataX_fore(:)', dataX_ibtw(:)']) > 0)
                
                mkdir('D-ft')
                figureNameFT = strcat('D-ft/',package,'-',dbName,'-aggr',num2str(aggregatedTime), '-scatter-ft.pdf');
                % figureNameFB = strcat('figures/',packageName,'-',dbName,'-aggr',num2str(aggregatedTime), '-bar-bt.pdf');
                fig_FT = figure('units','normalized','outerposition',[0 0 1 1],'visible',visible);
                
                dataX_plot = [dataX{dbIdx,:,packageIdx}];
                dataY_plot = datenum(minutes([dataY{dbIdx,:,packageIdx}]));
                

                
                if(aggregatedTime > 1)
                    firstDayDN = min(dataX_plot);
                    if(numel(firstDayDN) == 0)
                        firstDayDN = 0; %in case all dataset are empty
                    end
                    firstDayDT = datetime(firstDayDN,'ConvertFrom', 'datenum');
                    firstDayDT.Second = 0;
                    firstDayDT.Minute = 0;
                    firstDayDT.Hour = 0;
                    firstDayDN = datenum(firstDayDT);
                    
                    
                    lastDayDN = max(dataX_plot);
                    lastDayDT = datetime(lastDayDN,'ConvertFrom', 'datenum');
                    lastDayDT.Second = 0;
                    lastDayDT.Minute = 0;
                    lastDayDT.Hour = 0;
                    lastDayDT.Day = lastDayDT.Day + 1;
                    lastDayDN = datenum(lastDayDT);
                    
                    %transform aggregatedTime into datenum
                    
                    [dataX_plot, dataY_plot] = aggregate(dataX_plot, dataY_plot, aggregatedTimeDN, firstDayDN);                 
                end
                
                maxYaxis = max(dataY_plot) + datenum(seconds(1));
                
                                resultName = sqlite3.execute(database, strcat('SELECT app_name from table_installed_apps WHERE package_name =''', package,''''));
                appname = resultName(1).app_name;
                
                scatter(dataX_plot, dataY_plot, 'filled') %plotBar(dataX_back, dataY_back, dataX_fore, dataY_fore, dataX_ibtw, dataY_ibtw, showParam, logYaxis, 'off');
                title(strcat(appname, {' - Time usage for '},dbName, {'  - aggregation by '}, num2str(aggregatedTime), {' min'}),'FontSize',titleFontSize);
                
                ylabel('Time used [hh:mm(:ss)]','FontSize',axisLabelFontSize);
                set(gca, 'FontSize', axisLabelFontSize);
                grid on;
                
                ylim([minYaxis, maxYaxis]);
                dynamicDateTicksY();
                dynamicDateTicks();
                
                %datetick('y','MM:SS');
                axx=gca;
                axx.XTickLabelRotation = 45;
                %dynamicDateTicks(axH, 'link', 'dd/mm')
                saveTightFigure(fig_FT, figureNameFT);
                display(strcat({'plotting ' }, dbName, {': '}, num2str(packageIdx),'/',num2str(numel(packagesNames)), {' packages processed'}));
              
            end
        end
    end
end
