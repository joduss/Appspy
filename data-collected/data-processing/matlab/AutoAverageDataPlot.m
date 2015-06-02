clear;
%% PARAMETERS
packages = {'com.facebook.katana', 'com.whatsapp'}; %apps to plota
dbDirectory = 'db';
dbFiles = dir(strcat(dbDirectory, '/db*.db'));
dbFilePaths = {};

logYaxis = 1; %display log scale? (1 = true)
aggregatedTime = 60; %1 = no aggregation
visible = 'on';

%% preprocessing
databases = [];
aggregatedTimeDN = datenum(minutes(aggregatedTime));

%load db and find max/min time for all db
min_millis = Inf;
max_millis = 0;

for idx = 1 : numel(dbFiles)
    dbFilePaths{idx} = strcat(dbDirectory, '/', dbFiles(idx).name);
    databases(idx) = sqlite3.open(dbFilePaths{idx});
    
    %find min/max record_time
    results = sqlite3.execute(databases(idx), strcat('SELECT min(record_time) as min_rt, max(record_time) as max_rt from table_applications_activity'));
    if(results(1).min_rt < min_millis)
        min_millis = results(1).min_rt;
    end
    if(results(1).max_rt > max_millis)
        max_millis = results(1).max_rt;
    end
    
end


%setup last/first record time
beginTS = (min_millis+2*60*60000)/1000;
beginDT = datetime([1970 1 1 0 0 beginTS]);
beginDT.Second = 0; %round time as record may have + 1 to 10 seconds of delay
beginDT.Minute = 0;
beginDT.Hour = 0;
beginDN = datenum(beginDT);


endTS = (max_millis+2*60*60000)/1000;
endDT = datetime([1970 1 1 0 0 endTS]);
endDT.Second = 0;
endDT.Minute = 0;
endDT.Hour = 0;
endDT.Day = endDT.Day + 1;
endDN = datenum(endDT);





%% processing

nb = minutes(endDT - beginDT) / aggregatedTime;
%rows = db
%col = pkg
%depth = records
dataX_up = cell(numel(dbFiles), numel(packages), nb);
dataX_up(:) = deal({0});
dataY_up = cell(numel(dbFiles), numel(packages), nb);
dataY_up(:) = deal({0});

dataX_down = cell(numel(dbFiles), numel(packages), nb);
dataX_down(:) = deal({0});
dataY_down = cell(numel(dbFiles), numel(packages), nb);
dataY_down(:) = deal({0});

timeX=cell(numel(dbFiles), numel(packages), nb);
timeX(:) = deal({0});
timeY=cell(numel(dbFiles), numel(packages), nb);
timeY(:) = deal({0});

clear('results');

for dbIdx = 1:numel(databases)
    database = databases(dbIdx);
    [~,dbName,~] = fileparts(dbFilePaths{dbIdx});
    

     for pkgIdx = 1:numel(packages);
         package = packages{pkgIdx};
         
         dataResults = sqlite3.execute(database, strcat('SELECT * from table_applications_activity WHERE package_name =''', package, ''' AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) ORDER BY record_time'));
         timeResults = sqlite3.execute(database, strcat('SELECT * from table_applications_activity WHERE package_name =''', package, ''' AND was_foreground=1 order by record_time'));
%         
%         %% Process data usage
%         %data results processing
%         %up/down value < 0 set to 0
%         if(numel(dataResults) > 0)
%             idx = find([dataResults.uploaded_data] < 0);
%             a = mat2cell(zeros(1,numel(idx))+5, 1, ones(1,numel(idx))) ;
%             [dataResults(idx).uploaded_data] = a{:};
%             
%             
%             %         idx=1;
%             %         aggr_up = 0;
%             %         aggr_down = 0;
%             %         currentTimeIntervalBegin = aggregatedTimeDN + begindDN
%             
%             dataX = arrayfun(@millisToDatenumRoundSec, [dataResults.record_time]);
% 
%                         
%             %for upload
%             dataY = [dataResults.uploaded_data]/1024;
%             
%             [dataX_aggr, dataY_aggr] = aggregate(dataX, dataY, aggregatedTimeDN, beginDN);
% 
%             dataX_up(dbIdx,pkgIdx,1:numel(dataX_aggr)) = num2cell(dataX_aggr);
%             dataY_up(dbIdx,pkgIdx,1:numel(dataY_aggr)) = num2cell(dataY_aggr);
%             
%             %for download
%             dataY = [dataResults.downloaded_data]/1024;
%             
%             [dataX_aggr, dataY_aggr] = aggregate(dataX, dataY, aggregatedTimeDN, beginDN);
%             
%             dataX_down(dbIdx,pkgIdx,1:numel(dataX_aggr)) = num2cell(dataX_aggr);
%             dataY_down(dbIdx,pkgIdx,1:numel(dataY_aggr)) = num2cell(dataY_aggr);
%             
%             
%         end
        
        %% Process time usage
        if(numel(timeResults) > 0)
            
            timeX_proc = [];
            timeY_proc = [];
            lastFT = 0;
            %process time usage, store in in cell
            for(recordIdx = 1:numel(timeResults))
                rec = timeResults(recordIdx);
                time = millisToDatenumRoundSec(rec.record_time);               
                
                timeX_proc(recordIdx) = time;
                ft = rec.foreground_time_usage /1000 / 60; %in minutes
                if(ft < lastFT)
                    lastFT = 0;
                end
                timeY_proc(recordIdx) = ft - lastFT;
                lastFT = ft;
            end                   
                        
            [timeX_aggr, timeY_aggr] = aggregate(timeX_proc, timeY_proc, aggregatedTimeDN, beginDN);
            
            timeX(dbIdx,pkgIdx,1:numel(timeX_aggr)) = num2cell(timeX_aggr);
            timeY(dbIdx,pkgIdx,1:numel(timeY_aggr)) = num2cell(timeY_aggr);
        end
        
        
    end
    
    
    
    
end

%% PLOT DATA
for pkgIdx = 1 : numel(packages)

    close all;
    package = packages{pkgIdx};
    
    
    %generate a fake graph just to take the X axis label for the boxplot
    %later
    figureForLabels = figure('units','normalized','outerposition',[0 0 1 1],'visible','off');
    scatter([dataX_up{1,1,:}], [dataY_up{1,1,:}]);
    xlim([beginDN endDN]);
    ax = gca;
    dynamicDateTicks(ax);
    
    % postprocess data (remove line only zero, as has not used the app)
    %only consider for user that used the app
    
    %%upload data
    dataY_plot = reshape([dataY_up{:,pkgIdx,:}],[numel(databases) numel([dataY_up{1,pkgIdx,:}])]);
    idxNotZero = find(sum(dataY_plot,2) > 0);
    dataY_plot = dataY_plot(idxNotZero,:);
    
    fig_up = figure('units','normalized','outerposition',[0 0 1 1],'visible',visible);       
    boxplot(dataY_plot);
    title(strcat('upload-',package,'-','aggr=',num2str(aggregatedTime)));
       
    copySetAxis(ax, gca, logYaxis);    
    pause;
    close(fig_up);

    %download data
    dataY_plot = reshape([dataY_down{:,pkgIdx,:}],[numel(databases) numel([dataY_down{1,pkgIdx,:}])]);
    idxNotZero = find(sum(dataY_plot,2) > 0);
    dataY_plot = dataY_plot(idxNotZero,:);
    
    fig_down = figure('units','normalized','outerposition',[0 0 1 1],'visible',visible);       
    boxplot(dataY_plot);
    title(strcat('download-',package,'-','aggr=',num2str(aggregatedTime)));
       
    copySetAxis(ax, gca, logYaxis);
    pause;
    close(fig_down);
    
    %% time usage
    dataY_plot = reshape([timeY{:,pkgIdx,:}],[numel(databases) numel([timeY{1,pkgIdx,:}])]);
    idxNotZero = find(sum(dataY_plot,2) > 0);
    dataY_plot = dataY_plot(idxNotZero,:);
    
    fig_FT = figure('units','normalized','outerposition',[0 0 1 1],'visible',visible);       
    boxplot(dataY_plot);
    
    
    title(strcat('usagetime-',package,'-','aggr=',num2str(aggregatedTime)));
       
    ax.XTick
    copySetAxis(ax, gca, 0);
        ylim([0, aggregatedTime]);

    pause;
end


