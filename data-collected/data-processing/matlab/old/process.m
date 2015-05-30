%mex -setup C++;
%qlite3.make

dbnames = {'db1.db','db3.db'};
packageName = 'com.google.android.gms';
wasForeground = 0;
type = 'bar'; %type = 'bar' or 'point'
log = 1; %display log scale? (1 = true)
subplotLayout = 'h'; %vertical = v, horizontal = h
axisLink = 'xy'; %x, y or xy



ax = zeros(1,numel(dbnames));

close all;
figure('name',strcat(packageName));
for nameIdx = 1 : numel(dbnames)
    dbname = dbnames{nameIdx};
    database = sqlite3.open(dbname);
    results = sqlite3.execute(database, 'SELECT * from table_applications_activity WHERE package_name = ? AND uploaded_data>0 AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) ORDER BY record_time', packageName);
    
    
    dataX = zeros(numel(results),1);
    dataY = zeros(numel(results),1);
    
    for rowIdx = 1:numel(results)
        if(results(rowIdx).uploaded_data >= 0)
            dataY(rowIdx) = results(rowIdx).uploaded_data/1024.0;
            timestamp = results(rowIdx).record_time/1000;
            
            time = [1970 1 1 0 0 timestamp];
            t = datetime(time,'InputFormat','dd-MMM-yyyy HH:mm:ss');
            t.Second = 0;
            time = datenum(t);
            
            dataX(rowIdx) = time;
        end
    end
    
    %clean: remove where record_time = 0
    idxZero = find(dataX ~= 0);
    dataX = dataX(idxZero);
    dataY = dataY(idxZero);
    
    %%
    if(strcmp(subplotLayout,'v'))
        ax(nameIdx) = subplot(numel(dbnames),1,nameIdx);
    else
        ax(nameIdx) = subplot(1,numel(dbnames),nameIdx);
    end
    
    p = get(ax(nameIdx), 'pos');
    p(3) = p(3) + 0.05/numel(dbnames);
    p(1) = p(1) - 0.05/numel(dbnames);
     set(ax(nameIdx), 'pos', p);
    
    
    if(strcmp('type', 'bar'))
        bar(dataX, dataY,'BarWidth',1);
        %bar(dataY,1);
        
        dynamicDateTicks();
    else
        s = scatter(dataX,dataY,'filled');
        dynamicDateTicks();
    end
    
    if(log == 1)
        set(gca,'YScale','log');
    end
    title(dbname);
    ylabel('data uploaded [kB]')
end

%ling axis together (time does not matter, only data)
linkaxes(ax,axisLink);



%dynamicDateTicks
%subplot(2,1,2), plot(dates, Signal4);


%figure
%ax1 = subplot(2,1,1); bar(dataX, dataY);
%ax2 = subplot(2,1,2); plot(dates, Signal4);
%linkaxes([ax1], 'x');
%dynamicDateTicks([ax1], 'linked')
