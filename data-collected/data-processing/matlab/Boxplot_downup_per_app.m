clear;
%% PARAMETERS
packages = {'com.facebook.katana', 'com.facebook.orca','com.whatsapp', 'com.kitkatandroid.keyboard', 'com.google.android.gm','com.google.android.gms', 'com.zoodles.kidmode', 'com.google.android.youtube', 'com.google.android.talk','com.google.android.googlequicksearchbox','com.google.android.apps.plus', 'com.skype.raider', 'com.viber.voip',  }; %apps to plota
dbDirectory = 'db';
dbFiles = dir(strcat(dbDirectory, '/db*.db'));
dbFilePaths = {};

logYaxis = 1; %display log scale? (1 = true)
aggregatedTime = 10; %1 = no aggregation
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
        
        if(numel(dataResults) > 0)
            dataDownY = [dataResults.downloaded_data] / 1024;
            dataUpY = [dataResults.uploaded_data] / 1024;
            dataX = arrayfun(@millisToDatenumRoundSec, [dataResults.record_time]);


            [dataX_aggr, dataDownY_aggr] = aggregate(dataX, dataDownY, aggregatedTimeDN,minDN );
            [dataX_aggr, dataUpY_aggr] = aggregate(dataX, dataUpY, aggregatedTimeDN,minDN );
            
            %only keep when not 0 (=10^(-10)) after aggregation (because
            %already adapted for log)
            
            dataDownY_aggr = dataDownY_aggr(dataDownY_aggr > 10^(-10));
            dataUpY_aggr = dataUpY_aggr(dataUpY_aggr > 10^(-10));
            
            resultName = sqlite3.execute(database, strcat('SELECT app_name from table_installed_apps WHERE package_name =''', package,''''));
            appname = resultName(1).app_name;
            
            boxplotDataY = [boxplotDataY dataDownY_aggr(:)' dataUpY_aggr(:)'];
            group = [group, repmat(cellstr(strcat(appname, {' - download'})), numel(dataDownY_aggr), 1)', ...
                repmat(cellstr(strcat(appname, {' - upload'})), numel(dataUpY_aggr), 1)'];
            
            
        end
    end
    
    %avoid problem with log
    if(logYaxis == 1)
        boxplotDataY(boxplotDataY <= 0) = 10^(-10);
    else
        boxplotDataY(boxplotDataY<0) = 0;
        boxplotDataY(boxplotDataY==10^(-10)) = 0;
    end
    figureForLabels = figure('units','normalized','outerposition',[0 0 1 1],'visible','off');
    boxplot(boxplotDataY, group);
    
    if(logYaxis == 1)
        set(gca,'YScale','log');
        ylim([10^(-2) 10^6]);
    end
    
    
    ticksFontSize = 30;
                titleFontSize = 34;
                axisLabelFontSize = 30;
    
    title(dbName, 'FontSize',titleFontSize);
    ylabel('data[kb]');
    ax=gca;
    ax.XTickLabelRotation = 45;
    set(gca, 'FontSize', ticksFontSize);
    
    mkdir('D_figure_boxplot');
    saveTightFigure(strcat('D_figure_boxplot/','boxplot-',dbName,'-aggr-',num2str(aggregatedTime),'.pdf'));
    
    %pause;
    
end



