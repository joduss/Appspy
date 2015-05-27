function [fig ] = plotDataPoint( dataX_back, dataY_back, dataX_fore, dataY_fore, dataX_ibtw, dataY_ibtw,...
    showParam, logYaxis, visible )
%UNTITLED3 Summary of this function goes here
%   Detailed explanation goes here

fig = figure('units','normalized','outerposition',[0 0 1 1],'visible',visible);

hold all;
legendText = {};

%add data to figure for upload
if(strcmp(showParam,'b') || strcmp(showParam,'a'))
    scatter(dataX_back,dataY_back,50,'filled','m');
    legendText = [legendText 'Background'];
end
if(strcmp(showParam,'f') || strcmp(showParam,'a'))
    scatter(dataX_fore,dataY_fore,50,'filled','g');
    legendText = [legendText 'Foreground'];
end
scatter(dataX_ibtw,dataY_ibtw,50,'filled','b');
legendText = [legendText 'Inbetween'];
legend(legendText,'FontSize',20);

hold off;

allXValues = [dataX_back', dataX_fore', dataX_back'];
minX = min(allXValues(allXValues > 0));
maxX = max(allXValues);



if(logYaxis == 1)
    set(gca,'YScale','log');
end

%set axis limit
if(minX ~=maxX)
    addTime = datetime([0 0 0 1 0 0],'InputFormat','dd-MMM-yyyy HH:mm:ss');
    timeOffset = abs(datenum(addTime));
    xlim([minX - timeOffset, maxX + timeOffset]);

    
end
dynamicDateTicks();



end

