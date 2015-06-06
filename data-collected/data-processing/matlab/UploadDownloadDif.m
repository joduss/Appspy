
clear;
%% PARAMETERS
packages = {'com.facebook.katana', 'com.facebook.orca','com.whatsapp', 'com.kitkatandroid.keyboard', 'com.google.android.gm','com.google.android.gms', 'com.zoodles.kidmode', 'com.google.android.youtube', 'com.google.android.talk','com.google.android.googlequicksearchbox','com.google.android.apps.plus' }; %apps to plota

dbDirectory = 'db';
dbFiles = dir(strcat(dbDirectory, '/db*.db'));
dbFilePaths = {};

aggregatedTime = 10; %1 = no aggregation

%% preprocessing
databases = [];

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



%%

%cols = apps
%rows = record
%

nbInterval = (max_millis - min_millis)/1000/60 / aggregatedTime;
nbInterval = ceil(nbInterval);


for idx = 1 : numel(dbFiles)
    database = databases(idx);
    
    %dataUp = cell(numel(packages), nbInterval);
    %dataDown = cell(numel(packages), nbInterval);
    data = cell(nbInterval,numel(packages));
    header = cell(1,numel(packages));
    
    fileID = fopen(strcat(dbFiles(idx).name,'-aggr',num2str(aggregatedTime),'.dat'),'w');
    for pkgIdx = 1:numel(packages);
        package = packages{pkgIdx};
        
        packageHeader = strrep(package, '.', '_');
        header{pkgIdx} = packageHeader;
        
        dataResults = sqlite3.execute(database, strcat('SELECT * from table_applications_activity WHERE package_name =''', package, ''' AND was_foreground=0 AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) ORDER BY record_time'));
        if(numel(dataResults) > 0)
            dataX = arrayfun(@millisToDatenumRoundSec, [dataResults.record_time]);
            [dataX_aggr, dataUpY_aggr] = aggregate(dataX, [dataResults.uploaded_data], datenum(minutes(aggregatedTime)), millisToDatenumRoundSec(min_millis) );
            [dataX_aggr, dataDownY_aggr] = aggregate(dataX, [dataResults.downloaded_data], datenum(minutes(aggregatedTime)), millisToDatenumRoundSec(min_millis) );
            
            %dataUp(pkgIdx,1:numel(dataUpY_aggr)) = num2cell(dataUpY_aggr);
            %dataDown(pkgIdx,1:numel(dataDownY_aggr)) = num2cell(dataDownY_aggr);
            
            
            dif = dataDownY_aggr - dataUpY_aggr;
            dif = [dif(:); zeros(nbInterval-numel(dif),1)];
            dif = dif(:);
            %data{1,pkgIdx} = package;
            data(:,pkgIdx) = num2cell(dif(:)');
        end
        
        
        for index = 1:numel([data{:,pkgIdx}])
            fprintf(fileID,strcat(package,',',num2str(data{index,pkgIdx}),'\n\r'));
        end
        
    end
    fclose(fileID);
    % csvwrite(strcat(dbFiles(idx).name,'-aggr',num2str(aggregatedTime)),data);
    
    %T = cell2table(data,'VariableNames',header);
    %writetable(T,strcat(dbFiles(idx).name,'-aggr',num2str(aggregatedTime),'.csv'))
    
    
    
    
    %     %header = [];
    %     for pkgIdx = 1:numel(packages);
    %         %[~,dbName,~] = fileparts(dbFilePaths{dbIdx});
    %
    %         down = dataDown{pkgIdx, :};
    %         up = dataUp{pkgIdx, :};
    %
    %
    %     end
    
    
    
end





