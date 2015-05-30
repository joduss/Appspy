%mex -setup C++;
%qlite3.make


%PLOT FORE + BACK on the same plot
% + possibility to have diff color for "in between"
clear all;
dbDirectory = 'db';
dbnames = dir(strcat(dbDirectory, '/db*.db'));

type = 'point'; %type = 'bar' or 'point'
log = 1; %display log scale? (1 = true)
axisLink = 'xy'; %x, y or xy
show = 'a'; %a = all, b = back, f = fore
offset=3;
aggregateTime = 1; %1 = no aggregation

%TO ADAPT FIGURE SIZE: LOOK FOR "f = figure"



close all;
for nameIdx = 1 : numel(dbnames)
    dbname = strcat(dbDirectory, '/', dbnames(nameIdx).name);
    database = sqlite3.open(dbname);
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
                dataY_down_back = results(rowIdx).downloaded_data/1024.0;
                dataX_down_back(rowIdx) = time;
            elseif(row.was_foreground == 1)
                dataY_fore(rowIdx) = row.uploaded_data/1024.0;
                dataX_fore(rowIdx) = time;
                dataY_down_fore = results(rowIdx).downloaded_data/1024.0;
                dataX_down_fore(rowIdx) = time;
            elseif(results(rowIdx).uploaded_data > 0)
                dataY_ibtw(rowIdx) = row.uploaded_data/1024.0;
                dataX_ibtw(rowIdx) = time;
                dataY_down_ibtw = results(rowIdx).downloaded_data/1024.0;
                dataX_down_ibtw(rowIdx) = time;
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
        
        %clean the same way, but for down
        idxZero = find(dataX_down_down_down_back ~= 0);
        dataX_down_back = dataX_down_back(idxZero);
        dataY_down_back = dataY_down_back(idxZero);

        idxZero = find(dataX_down_fore ~= 0);
        dataX_down_fore = dataX_down_fore(idxZero);
        dataY_down_fore = dataY_down_fore(idxZero);

        idxZero = find(dataX_down_ibtw ~= 0);
        dataX_down_ibtw = dataX_down_ibtw(idxZero);
        dataY_down_ibtw = dataY_down_ibtw(idxZero);

        %%
        close all;
        f_uploaded = figure('units','normalized','outerposition',[0 0 1 1],'visible','off');
        f_downloaded = figure('units','normalized','outerposition',[0 0 1 1],'visible','off');

        if(strcmp(type, 'bar'))
            bar(dataX_back, dataY_back,'BarWidth',1);
            %bar(dataY,1);

        else
            hold all;
            legendText = {};
            if(strcmp(show,'b') || strcmp(show,'a'))
                scatter(dataX_back,dataY_back,50,'filled','m');              
                legendText = [legendText 'Background'];
            end
            if(strcmp(show,'f') || strcmp(show,'a'))
                scatter(dataX_fore,dataY_fore,50,'filled','g');
                legendText = [legendText 'Foreground'];
            end
            scatter(dataX_ibtw,dataY_ibtw,50,'filled','b');
            legendText = [legendText 'Inbetween'];
            legend(legendText,'FontSize',20);
            
            allXValues = [dataX_back', dataX_fore', dataX_back', dataX_down_back', dataX_down_fore', dataX_down_ibtw'];
            minX = min(allXValues(allXValues > 0));
            maxX = max(allXValues);

            hold off;
            
        end
        
        if(log == 1)
            set(gca,'YScale','log');
        end
        title(strcat(dbname,{' - '}, packageName),'FontSize',25);
        ylabel('data uploaded [kB]','FontSize',22);
        set(gca, 'FontSize', 22);
        grid on;

        dynamicDateTicks();
        
                %set axis limit
        if(minX ~=maxX)
           xlim([minX - (maxX-minX)*0.05, maxX + (maxX-minX)*0.05]);
        end
        
        %save figure and remove borders
        [~,dbfilename,~] = fileparts(dbname);
        figureNameUpload = strcat('figures/',dbfilename,'-',packageName, '-upload.pdf');
        saveTightFigure(f_uploaded, figureNameUpload);
        figureNameDownload = strcat('figures/',dbfilename,'-',packageName, '-download.pdf');
        saveTightFigure(f_downloaded, figureNameDownload);

        %display some stats:
        display(strcat({'###### '}, dbname));
        display(strcat({'Uploaded data on background: '}, num2str(sum(dataY_back))));
        display(strcat({'Uploaded data on foreground: '}, num2str(sum(dataY_fore))));
        display(strcat({'Uploaded data on iwbt: '}, num2str(sum(dataY_ibtw))));
        
        
        %pause;
        close all;
        display('next');

    end
end

