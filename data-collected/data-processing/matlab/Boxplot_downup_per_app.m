clear;
%% PARAMETERS
packages = {'com.facebook.katana', 'com.whatsapp'}; %apps to plota
dbDirectory = 'db';
dbFiles = dir(strcat(dbDirectory, '/db*.db'));
dbFilePaths = {};

logYaxis = 0; %display log scale? (1 = true)
aggregatedTime = 1; %1 = no aggregation
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

minDN = millisToDatenumRoundSec(min_millis);
maxDN = millisToDatenumRoundSec(max_millis);


%% processing



clear('results');
close all;
for dbIdx = 1:numel(databases)
    database = databases(dbIdx);
    [~,dbName,~] = fileparts(dbFilePaths{dbIdx});
    boxplotDataY = [];
    group = [];
    
    for pkgIdx = 1:numel(packages);
        package = packages{pkgIdx};
        
        dataResults = sqlite3.execute(database, strcat('SELECT * from table_applications_activity WHERE package_name =''', package, ''' AND was_foreground=0 AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) ORDER BY record_time'));
        %timeResults = sqlite3.execute(database, strcat('SELECT * from table_applications_activity WHERE package_name =''', package, ''' AND was_foreground=1 order by record_time'));
        
        dataDownY = [dataResults.downloaded_data] / 1024;
        dataUpY = [dataResults.uploaded_data] / 1024;
        dataX = arrayfun(@millisToDatenumRoundSec, [dataResults.record_time]);
        
        
        [dataX_aggr, dataDownY_aggr] = aggregate(dataX, dataDownY, aggregatedTimeDN,minDN );
        [dataX_aggr, dataUpY_aggr] = aggregate(dataX, dataUpY, aggregatedTimeDN,minDN );
        
        
        boxplotDataY = [boxplotDataY dataDownY_aggr(:)' dataUpY_aggr(:)'];
        group = [group, repmat(cellstr(strcat(package, '-download')), numel(dataDownY_aggr), 1)', ...
            repmat(cellstr(strcat(package, '-upload')), numel(dataUpY_aggr), 1)'];
        
    end
    
    %avoid problem with log
    if(logYaxis == 1)
        boxplotDataY(boxplotDataY <= 0) = 10^(-10);
    else
        boxplotDataY(boxplotDataY<0) = 0
        boxplotDataY(boxplotDataY==10^(-10)) = 0
    end
    
    boxplot(boxplotDataY, group);
    
    if(logYaxis == 1)
        set(gca,'YScale','log');
        ylim([10^(-2) 10^6]);
    end
    
    title(dbName);
    ylabel('data[kb]');
    
    pause;
    
end



