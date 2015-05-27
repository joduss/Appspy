function [ minY_upload, maxY_upload, minY_download, maxY_download, globalYmin, globalYmax ] = findCommonAxisLimits( databases, packageName )
%UNTITLED4 Summary of this function goes here
%   Detailed explanation goes here

minY_upload = Inf;
maxY_upload = 0;
minY_download = Inf;
maxY_download = 0;


for database = databases
    results_up = sqlite3.execute(database, strcat('SELECT min(uploaded_data/1024) as min_up, max(uploaded_data/1024) as max_up from table_applications_activity WHERE package_name =''', packageName, ''' AND uploaded_data>0 AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1) '));
    results_down = sqlite3.execute(database, strcat('SELECT min(downloaded_data/1024) as min_down, max(downloaded_data/1024) as max_down from table_applications_activity WHERE package_name =''', packageName, ''' AND downloaded_data>0 AND record_time > (SELECT record_time from table_applications_activity where record_id=1 limit 1)'));
    
    
    if(numel(results_up) == 1)
        if(results_up.min_up < minY_upload)
            minY_upload = results_up.min_up;
        end            
        if(results_up.max_up > maxY_upload)
            maxY_upload = results_up.max_up;
        end
    end
    
    if(numel(results_down) == 1)
        if(results_down.min_down < minY_download)
            minY_download = results_down.min_down;
        end
        if(results_down.max_down > maxY_download)
            maxY_download = results_down.max_down;
        end
    end
    
    %%if no record at the end
    if(minY_download == Inf)
        minY_download = 0;
        maxY_download = 1;
    end
    if(minY_upload == Inf)
        minY_upload = 0;
        maxY_upload = 1;
    end
    
    
    globalYmin = min(minY_download, minY_upload);
    globalYmax = max(maxY_download, maxY_upload);
    
    
    
    
end


end

