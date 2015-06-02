%mex -setup C++;
%qlite3.make

function[] = statsFunc(packageName, offset, dbFilePaths)
%Compute some statistics about fownload, upload and how many times used


%% PROCESSING
databases = ones(1,numel(dbFilePaths));
for nameIdx = 1 : numel(dbFilePaths)
    databases(nameIdx) = sqlite3.open(dbFilePaths{nameIdx});
end


close all;
display('#######################################################################################')
display(strcat(packageName, '\n'))
for nameIdx = 1 : numel(dbFilePaths)
    [~,dbName,~] = fileparts(dbFilePaths{nameIdx});
    database = databases(nameIdx);
    results = sqlite3.execute(database, 'SELECT * from table_applications_activity WHERE package_name = ? AND uploaded_data>0 AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) ORDER BY record_time', packageName);
    
    results2 = sqlite3.execute(database, 'SELECT sum(was_foreground) from table_applications_activity WHERE package_name = ? AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) ORDER BY record_time', packageName);

    
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
        
    %Skip first one: only used for time reference as we don't know
    %what there were  before (foreground or not?) Assure foreground.
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
            dataY_back(rowIdx) = row.uploaded_data/1024.0;
            dataX_back(rowIdx) = time;
        elseif(row.was_foreground == 1)
            dataY_fore(rowIdx) = row.uploaded_data/1024.0;
            dataX_fore(rowIdx) = time;
        elseif(row.was_foreground == 0)
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
    
   
    
    
    
    
    %display some stats:
    display(strcat({'###### '}, dbName));
    display(strcat({'sum_was_foreground (nb records where app was open): '}, num2str(results2.sum_was_foreground)));
    display(strcat({'Uploaded data on background: '}, num2str(sum(dataY_back))));
    display(strcat({'Uploaded data on foreground: '}, num2str(sum(dataY_fore))));
    display(strcat({'Uploaded data on iwbt: '}, num2str(sum(dataY_ibtw))));


    
    
end

