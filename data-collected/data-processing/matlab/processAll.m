%mex -setup C++;
%qlite3.make


%PLOT FORE + BACK on the same plot
% + possibility to have diff color for "in between"


dbnames = {'db2.db'};
packageName = 'com.google.android.talk';
type = 'bar'; %type = 'bar' or 'point'
log = 1; %display log scale? (1 = true)
subplotLayout = 'h'; %vertical = v, horizontal = h
axisLink = 'xy'; %x, y or xy
show = 'a'; %a = all, b = back, f = fore
offset=3;



ax = zeros(1,numel(dbnames));

close all;
for nameIdx = 1 : numel(dbnames)
    dbname = dbnames{nameIdx};
    database = sqlite3.open(dbname);
    results = sqlite3.execute(database, 'SELECT * from table_applications_activity WHERE package_name = ? AND uploaded_data>0 AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) ORDER BY record_time', packageName);
    
    
    dataX_back = zeros(numel(results),1);
    dataY_back = zeros(numel(results),1);
    
    dataX_fore = zeros(numel(results),1);
    dataY_fore = zeros(numel(results),1);
    
    dataX_ibtw = zeros(numel(results),1);
    dataY_ibtw = zeros(numel(results),1);
    
    lastStatus = ones(1,offset);
    lastRow = cell(1,offset);
    for i = 1:offset
        if(numel(results) > 0)
            lastRow{i} = results(1);
        end
    end
    
    for rowIdx = 1:numel(results)
        timestamp = (results(rowIdx).record_time+2*60*60000)/1000;
        time = [1970 1 1 0 0 timestamp];
        t = datetime(time,'InputFormat','dd-MMM-yyyy HH:mm:ss');
        t.Second = 0;
        time = datenum(t);
        
        row = results(rowIdx);
        
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
        elseif(row.was_foreground == 1)
            dataY_fore(rowIdx) = row.uploaded_data/1024.0;
            dataX_fore(rowIdx) = time;
        elseif(results(rowIdx).uploaded_data > 0)
            dataY_ibtw(rowIdx) = row.uploaded_data/1024.0;
            dataX_ibtw(rowIdx) = time;
        end
        lastStatus(mod(rowIdx,offset)+1) = row.was_foreground;
        lastRow{mod(rowIdx,offset)+1} = row;
    end
    
    %clean: remove where record_time = 0
    idxZero = find(dataX_back ~= 0);
    dataX_back = dataX_back(idxZero);
    dataY_back = dataY_back(idxZero);
    
    idxZero = find(dataX_fore ~= 0);
    dataX_fore = dataX_fore(idxZero);
    dataY_fore = dataY_fore(idxZero);
    
    idxZero = find(dataX_ibtw ~= 0);
    dataX_ibtw = dataX_ibtw(idxZero);
    dataY_ibtw = dataY_ibtw(idxZero);
    
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
        bar(dataX_back, dataY_back,'BarWidth',1, 'stacked');
        %bar(dataY,1);
        
    else
        hold all;
        legendText = {};
        if(strcmp(show,'b') || strcmp(show,'a'))
            scatter(dataX_back,dataY_back,'filled','m');
            legendText = [legendText 'Background'];
        end
        if(strcmp(show,'f') || strcmp(show,'a'))           
            scatter(dataX_fore,dataY_fore,'filled','g');
            legendText = [legendText 'Foreground'];
        end
        scatter(dataX_ibtw,dataY_ibtw,'filled','b');
        legendText = [legendText 'Inbetween'];
        legend(legendText);
        hold off;
        
        
        
        if(log == 1)
            set(gca,'YScale','log');
        end
        title(strcat(dbname,{' - '}, packageName));
        ylabel('data uploaded [kB]');
        dynamicDateTicks();

    end
    
    
    %display some stats:
    display(strcat({'###### '}, dbname));
    display(strcat({'Uploaded data on background: '}, num2str(sum(dataY_back))));
    display(strcat({'Uploaded data on foreground: '}, num2str(sum(dataY_fore))));
    display(strcat({'Uploaded data on iwbt: '}, num2str(sum(dataY_ibtw))));


    
    
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
