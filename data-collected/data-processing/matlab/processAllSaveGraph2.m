%mex -setup C++;
%qlite3.make


%PLOT FORE + BACK on the same plot
% + possibility to have diff color for "in between"
clear;
dbDirectory = 'db';
dbnames = dir(strcat(dbDirectory, '/db*.db'));

type = 'point'; %type = 'bar' or 'point'
logYaxis = 1; %display log scale? (1 = true)
axisLink = 'xy'; %x, y or xy
showParam = 'a'; %a = all, b = back, f = fore
offset=3;
aggregateTime = 1; %1 = no aggregation

%TO ADAPT FIGURE SIZE: LOOK FOR "f = figure"

databases = ones(1,numel(dbnames));
for nameIdx = 1 : numel(dbnames)
    dbPath = strcat(dbDirectory, '/', dbnames(nameIdx).name);
    databases(nameIdx) = sqlite3.open(dbPath);
end


close all;
for nameIdx = 1 : numel(dbnames)
    dbPath = strcat(dbDirectory, '/', dbnames(nameIdx).name);
    [~,dbName,~] = fileparts(dbPath);
    
    database = databases(nameIdx);
    packagesNames = allPackageName(database);
    
    for packageName = packagesNames
        packageName = packageName{1};               
                
        results = sqlite3.execute(database, strcat('SELECT * from table_applications_activity WHERE package_name =''', packageName, ''' AND uploaded_data>0 AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) ORDER BY record_time'));
        
        dataX_back = zeros(numel(results),1);
        dataY_back = zeros(numel(results),1);
        
        dataX_fore = zeros(numel(results),1);
        dataY_fore = zeros(numel(results),1);
        
        dataX_ibtw = zeros(numel(results),1);
        dataY_ibtw = zeros(numel(results),1);
        
        
        dataX_down_back = zeros(numel(results),1);
        dataY_down_back = zeros(numel(results),1);
        
        dataX_down_fore = zeros(numel(results),1);
        dataY_down_fore = zeros(numel(results),1);
        
        dataX_down_ibtw = zeros(numel(results),1);
        dataY_down_ibtw = zeros(numel(results),1);
        
        
        lastStatus = ones(1,offset);
        lastRow = cell(1,offset);
        for i = 1:offset
            if(numel(results) > 0)
                lastRow{i} = results(1);
            end
        end
        
        
        %PROCESS UPLOADED
        for rowIdx = 1:numel(results)
            timestamp = (results(rowIdx).record_time+2*60*60000)/1000;
            time = [1970 1 1 0 0 timestamp];
            t = datetime(time,'InputFormat','dd-MMM-yyyy HH:mm:ss');
            t.Second = 0;
            time = datenum(t);
            
            row = results(rowIdx);
            
            %remove the last foreground activity too old to have impact on
            %background activity
            for lastIdx = 1:offset
                r = lastRow{lastIdx};
                if isrow(r) && row.record_time - r.record_time > offset * 60000 + 30000
                    lastRow{lastIdx} = [];
                    lastStatus(lastIdx) = 0;
                end
            end
            
            
            if(row.was_foreground == 0 && sum(lastStatus) == 0)
                dataY_back(rowIdx) = results(rowIdx).uploaded_data/1024.0;
                dataX_back(rowIdx) = time;
                dataY_down_back(rowIdx) = results(rowIdx).downloaded_data/1024.0;
                dataX_down_back(rowIdx) = time;
            elseif(row.was_foreground == 1)
                dataY_fore(rowIdx) = row.uploaded_data/1024.0;
                dataX_fore(rowIdx) = time;
                dataY_down_fore(rowIdx) = results(rowIdx).downloaded_data/1024.0;
                dataX_down_fore(rowIdx) = time;
            elseif(results(rowIdx).uploaded_data > 0)
                dataY_ibtw(rowIdx) = row.uploaded_data/1024.0;
                dataX_ibtw(rowIdx) = time;
                dataY_down_ibtw(rowIdx) = results(rowIdx).downloaded_data/1024.0;
                dataX_down_ibtw(rowIdx) = time;
            end
            lastStatus(mod(rowIdx,offset)+1) = row.was_foreground;
            lastRow{mod(rowIdx,offset)+1} = row;
        end
        
        %clean: remove where record_time = 0
        idxZero = find(dataX_back ~= 0 & dataY_back ~= 0);
        dataX_back = dataX_back(idxZero);
        dataY_back = dataY_back(idxZero);
        
        idxZero = find(dataX_fore ~= 0 & dataY_fore ~= 0);
        dataX_fore = dataX_fore(idxZero);
        dataY_fore = dataY_fore(idxZero);
        
        idxZero = find(dataX_ibtw ~= 0 & dataY_ibtw ~= 0);
        dataX_ibtw = dataX_ibtw(idxZero);
        dataY_ibtw = dataY_ibtw(idxZero);
        
        %clean the same way, but for down
        idxZero = find(dataX_down_back ~= 0 & dataY_down_back ~= 0);
        dataX_down_back = dataX_down_back(idxZero);
        dataY_down_back = dataY_down_back(idxZero);
        
        idxZero = find(dataX_down_fore ~= 0 & dataY_down_fore ~= 0);
        dataX_down_fore = dataX_down_fore(idxZero);
        dataY_down_fore = dataY_down_fore(idxZero);
        
        idxZero = find(dataX_down_ibtw ~= 0 & dataY_down_ibtw ~= 0);
        dataX_down_ibtw = dataX_down_ibtw(idxZero);
        dataY_down_ibtw = dataY_down_ibtw(idxZero);
        
        %% NOW PLOT THE DATA
        close all;
        f_uploaded = figure('units','normalized','outerposition',[0 0 1 1],'visible','off');
        f_downloaded = figure('units','normalized','outerposition',[0 0 1 1],'visible','off');
        
        %find y axis limit, same for all graph to be able to compare
        [minY_upload, maxY_upload, minY_download, maxY_download, minY_global, maxY_global] = findCommonAxisLimits(databases, packageName);
        
        
        
        if(strcmp(type, 'bar'))
            bar(dataX_back, dataY_back,'BarWidth',1);
            %bar(dataY,1);
            
        else
            %save figure and remove borders
            figureNameUpload = strcat('figures/',dbName,'-',packageName, 'scatter-upload.pdf');
            figureNameDownload = strcat('figures/',dbName,'-',packageName, 'scatter-download.pdf');
            
            close all;
            fig_upload = plotDataPoint(dataX_back, dataY_back, dataX_fore, dataY_fore, dataX_ibtw, dataY_ibtw, showParam, logYaxis, 'off');
            title(strcat({'Uploaded data for '},dbName,'-',packageName),'FontSize',25);
            ylabel('Uploaded data [kB]','FontSize',22);
            set(gca, 'FontSize', 22);
            grid on;
            ylim([minY_global, maxY_global]);
            if(numel([dataX_back', dataX_fore', dataX_ibtw']) > 0)
                saveTightFigure(fig_upload, figureNameUpload);
            end
            
            close all;
            fig_download = plotDataPoint(dataX_down_back, dataY_down_back, dataX_down_fore, dataY_down_fore, dataX_down_ibtw, dataY_down_ibtw, showParam, logYaxis, 'off');
            title(strcat({'Downloaded data for '},dbName,'-',packageName),'FontSize',25);
            ylabel('Downloaded data [kB]','FontSize',22);
            set(gca, 'FontSize', 22);
            grid on;
            ylim([minY_global, maxY_global]);
            
            if(numel([dataX_down_back', dataX_down_fore', dataX_down_ibtw']) > 0)
                tada = numel([dataX_back', dataX_fore', dataX_ibtw'])
                saveTightFigure(fig_download, figureNameDownload);
            end
        end
        
        %save figure and remove borders
        
        %display some stats:
        display(strcat({'###### '}, dbName, '-', packageName));
        display(strcat({'Uploaded data on background: '}, num2str(sum(dataY_back))));
        display(strcat({'Uploaded data on foreground: '}, num2str(sum(dataY_fore))));
        display(strcat({'Uploaded data on iwbt: '}, num2str(sum(dataY_ibtw))));
        
        display('next');
        %pause;
        
    end
end

