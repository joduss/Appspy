function [ dataX_back_out, dataY_back_out, dataX_fore_out, dataY_fore_out, dataX_ibtw_out, dataY_ibtw_out ] = aggregateData( dataX_back, dataY_back, dataX_fore, dataY_fore, dataX_ibtw, dataY_ibtw, aggregatedTime )
%UNTITLED2
%aggregateTime in minutes
%IMPORTANT: NO TEST IF AGGR; TIME DIVIDE THE INTERVAL LENGTH

%create bins of length aggregateTime beginning at midnight (morning) of the
%day of the first record
%until day of last record

firstDayTS = (min([dataX_back(:)', dataY_fore(:)', dataY_ibtw(:)'])+2*60*60000)/1000;
firstDayDT = datetime([1970 1 1 0 0 firstDayTS]);
firstDay  = firstDayDT.Day;
firstDayDN = datenum(firstDayDT);

lastDayTS = (max([dataX_back(:)', dataY_fore(:)', dataY_ibtw(:)'])+2*60*60000)/1000;
lastDayDT = datetime([1970 1 1 0 0 lastDayTS]);
lastDay  = lastDayDT.Day;

timeIntervalLength = (firstDay - lastDay) * 24 * 60; %length interval in minutes

%nbSubIntervals = timeIntervalLength / aggregatedTime;




[dataX_back_out, dataY_back_out] = aggregate(dataX_back, dataY_back, aggregateTime, firstDayDN);
[dataX_fore_out, dataY_fore_out] = aggregate(dataX_fore, dataY_fore, aggregateTime, firstDayDN);
[dataX_ibtw_out, dataY_ibtw_out] = aggregate(dataX_ibtw, dataY_ibtw, aggregateTime, firstDayDN);


end


function[dataX_out, dataY_out] = aggregate(dataX, dataY, aggregatedTime, startTime)

%currentInterval is designed by its end time (record what happened in the
%past aggregatedTimeMinutes
currentInterval = firstDayDN + aggregatedTime;
currentInterval = startTime + aggregatedTime;

aggrData = 0;
while idx < numel(dataX_back)
    recordTime = dataX_back(idx);
    %     recordValue = dataY_back(idx);
    %     recordTimeTS = (recordTime+2*60*60000)/1000;
    %     recordTimeDT = datetime([1970 1 1 0 0 recordTimeTS]);
    
    if(recordTimeDT >= (currentInterval - aggregatedTime) && recordTimeDT < currentInterval )
        aggrData = aggrData + dataY_back;
    elseif aggrData > 0
        dataX_out = [dataXt aggrData];
        dataY_out = [dataY recordTime]
    end
    currentInterval = currentInterval + minutes(10);
end
end