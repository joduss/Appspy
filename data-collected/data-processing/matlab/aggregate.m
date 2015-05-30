function[dataX_out, dataY_out] = aggregate(dataX, dataY, aggregatedTime, startTime)

%currentInterval is designed by its end time (record what happened in the
%past aggregatedTimeMinutes
currentInterval = startTime + aggregatedTime;
dataX_out = [];
dataY_out = [];

aggrData = 0;
idx=1;

%need to be sure dataX is sorted
dataX = sort(dataX);


while idx <= numel(dataX)
    recordTime = dataX(idx);
%     fprintf(strcat('\nrecord time: ',datestr(recordTime)))
%     fprintf(strcat('\nfd:          ',datestr(currentInterval - aggregatedTime)))
%     fprintf(strcat('\ncurrent interval: ',datestr(currentInterval)))
%     fprintf('\n-------')
    
    if(recordTime >= (currentInterval - aggregatedTime) && recordTime < currentInterval )
        aggrData = aggrData + dataY(idx);
        idx = idx + 1;
        if(idx > numel(dataX))
            %mean we are processing the last record
            dataX_out = [dataX_out currentInterval];
            dataY_out = [dataY_out aggrData];
        end
    elseif aggrData > 0
        dataX_out = [dataX_out currentInterval];
        dataY_out = [dataY_out aggrData];
        currentInterval = currentInterval + aggregatedTime;
        aggrData=0;
    else
        dataX_out = [dataX_out currentInterval];
         %put some small value. Avoid problem with log, and small enough not to
         %interfere with the graph
        dataY_out = [dataY_out 10^(-10)]; 
        currentInterval = currentInterval + aggregatedTime;
        aggrData=0;
    end
end
end