clear;
%% PARAMETERS
packages = {'com.facebook.katana', 'com.whatsapp'}; %apps to plota
dbDirectory = 'db';
dbFiles = dir(strcat(dbDirectory, '/db*.db'));
dbFilePaths = {};

logYaxis = 0; %display log scale? (1 = true)
aggregatedTime = 60; %1 = no aggregation
visible = 'off';

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


%figure('Visible', 'on'); plot(xlim, Y(:,1)) ;ax = gca; ax.XTick = xlim; dynamicDateTicks(ax);
%figure(); boxplot(Y'); set(gca, 'XTickLabel',ax.XTickLabel)


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
timeX(:) = deal({0});

clear('results');

for dbIdx = 1:numel(databases)
    database = databases(dbIdx);
    [~,dbName,~] = fileparts(dbFilePaths{dbIdx});
    

    for pkgIdx = 1:numel(packages);
        package = packages{pkgIdx};
        
        dataResults = sqlite3.execute(database, strcat('SELECT * from table_applications_activity WHERE package_name =''', package, ''' AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) ORDER BY record_time'));
        timeResults = sqlite3.execute(database, strcat('SELECT * from table_applications_activity WHERE package_name =''', package, ''' AND was_foreground=1'));
        
        %% Process data usage
        %data results processing
        %up/down value < 0 set to 0
        if(numel(dataResults) > 0)
            idx = find([dataResults.uploaded_data] < 0);
            a = mat2cell(zeros(1,numel(idx))+5, 1, ones(1,numel(idx))) ;
            [dataResults(idx).uploaded_data] = a{:};
            
            
            %         idx=1;
            %         aggr_up = 0;
            %         aggr_down = 0;
            %         currentTimeIntervalBegin = aggregatedTimeDN + begindDN
            
            dataX = arrayfun(@millisToDatenumRoundSec, [dataResults.record_time]);

                        
            %for upload
            dataY = [dataResults.uploaded_data]/1024;
            
            [dataX_aggr, dataY_aggr] = aggregate(dataX, dataY, aggregatedTimeDN, beginDN);

            dataX_up(dbIdx,pkgIdx,1:numel(dataX_aggr)) = num2cell(dataX_aggr);
            dataY_up(dbIdx,pkgIdx,1:numel(dataY_aggr)) = num2cell(dataY_aggr);
            
            if(numel(dataX_aggr) > 321)
                aaaa = 1000;
            end
            
            %for download
            dataY = [dataResults.downloaded_data]/1024;
            
            [dataX_aggr, dataY_aggr] = aggregate(dataX, dataY, aggregatedTimeDN, beginDN);
            
            dataX_down(dbIdx,pkgIdx,1:numel(dataX_aggr)) = num2cell(dataX_aggr);
            dataY_down(dbIdx,pkgIdx,1:numel(dataY_aggr)) = num2cell(dataY_aggr);
            
            
            %         while drIdx < numel(drIdx) + 1
            %             dataR = dataResults(drIdx);
            %             time = millisToDatenumRoundSec(dataR.record_time);
            %
            %             if(time >= currentTimeIntervalBegin - begindDN && time < currentTimeIntervalBegin)
            %                 upload = dataR.uploaded_data/1024.0;
            %                 download = dataR.download_data/1024.0;
            %             elseif()
            %             end
            %
            %
            %
            %         end
        end
        
        %% Process time usage
        
        
        aaa = 10;
        
    end
    
    
    
    
end

%% PLOT DATA
for pkgIdx = 1 : numel(packages)
    package = packages{pkgIdx};
    
    data = reshape([dataY_up{:,pkgIdx,:}],[numel(databases) numel([dataY_up{1,pkgIdx,:}])]);
    boxplot(data)
    
end

