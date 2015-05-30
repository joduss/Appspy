function [ dataX_back_out, dataY_back_out, dataX_fore_out, dataY_fore_out, dataX_ibtw_out, dataY_ibtw_out ] = aggregateData( dataX_back, dataY_back, dataX_fore, dataY_fore, dataX_ibtw, dataY_ibtw, aggregatedTime )
%UNTITLED2
%aggregateTime in minutes
%IMPORTANT: NO TEST IF AGGR; TIME DIVIDE THE INTERVAL LENGTH

%create bins of length aggregateTime beginning at midnight (morning) of the
%day of the first record
%until day of last record

firstDayDN = min([dataX_back(dataX_back > 0)', dataX_fore(dataX_fore > 0)', dataX_ibtw(dataX_ibtw > 0)']);
if(numel(firstDayDN) == 0)
    firstDayDN = 0; %in case all dataset are empty
end
firstDayDT = datetime(firstDayDN,'ConvertFrom', 'datenum');
firstDayDT.Second = 0;
firstDayDT.Minute = 0;
firstDayDT.Hour = 0;
firstDayDN = datenum(firstDayDT);


lastDayDN = max([0 dataX_back(:)', dataX_fore(:)', dataX_ibtw(:)']);
lastDayDT = datetime(lastDayDN,'ConvertFrom', 'datenum');
lastDayDT.Second = 0;
lastDayDT.Minute = 0;
lastDayDT.Hour = 0;
lastDayDT.Day = lastDayDT.Day + 1;
lastDayDN = datenum(lastDayDT);

%transform aggregatedTime into datenum
aggregatedTimeDN = datenum(datetime([0,1,0,0,aggregatedTime,0]));

%timeIntervalLength = (firstDay - lastDay) * 24 * 60; %length interval in minutes

%nbSubIntervals = timeIntervalLength / aggregatedTime;



[dataX_back_out, dataY_back_out] = aggregate(dataX_back, dataY_back, aggregatedTimeDN, firstDayDN);
[dataX_fore_out, dataY_fore_out] = aggregate(dataX_fore, dataY_fore, aggregatedTimeDN, firstDayDN);
[dataX_ibtw_out, dataY_ibtw_out] = aggregate(dataX_ibtw, dataY_ibtw, aggregatedTimeDN, firstDayDN);


end


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
        dataY_out = [dataY_out 10^(-10)];
        currentInterval = currentInterval + aggregatedTime;
        aggrData=0;    end
end
end