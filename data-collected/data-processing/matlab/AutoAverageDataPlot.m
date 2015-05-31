
%% PARAMETERS
packagesNames = {'com.facebook.katana'}; %apps to plota
dbDirectory = 'db';
dbFiles = dir(strcat(dbDirectory, '/db*.db'));
dbFilePaths = {}

logYaxis = 0; %display log scale? (1 = true)
aggregatedTime = 120; %1 = no aggregation
visible = 'off';

%% preprocessing
dataX = {};
dataY = {};
databases = []

%rows = db
%col = pkg
%depth = records

%load db and find max/min time for all db
min_millis = Inf;
max_millis = 0;

for idx = 1 : numel(dbFiles)
    dbFilePaths{idx} = strcat(dbDirectory, '/', dbFiles(idx).name);
    databases(idx) = sqlite3.open(dbFilePaths{idx});
    
    %find min/max record_time
    results = sqlite3.execute(database, strcat('SELECT min(record_time) as min_rt, max(record_time) as max_rt from table_applications_activity ORDER BY record_time'));
    if(results(1).min_rt < min_millis)
        min_millis = results(1).min_rt;
    end
    if(results(1).max_rt > max_millis)
        max_millis = results(1).max_rt;
    end
    
end

%find min and max time for all db

%figure('Visible', 'on'); plot(xlim, Y(:,1)) ;ax = gca; ax.XTick = xlim; dynamicDateTicks(ax);
%figure(); boxplot(Y'); set(gca, 'XTickLabel',ax.XTickLabel)




