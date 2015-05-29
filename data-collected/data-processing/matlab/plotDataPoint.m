function [fig ] = plotDataPoint( dataX_back, dataY_back, dataX_fore, dataY_fore, dataX_ibtw, dataY_ibtw,...
    showParam, logYaxis, visible )
%UNTITLED3 Summary of this function goes here
%   Detailed explanation goes here

fig = figure('units','normalized','outerposition',[0 0 1 1],'visible',visible);

hold all;
legendText = {};

%add data to figure for upload
if(strcmp(showParam,'b') || strcmp(showParam,'a'))
    if(numel(dataX_back) > 0)
        scatter(dataX_back,dataY_back,50,'filled','m');
        legendText = [legendText 'Background'];
    end
    
end
if(strcmp(showParam,'f') || strcmp(showParam,'a'))
    if(numel(dataX_fore') > 0)
        scatter(dataX_fore,dataY_fore,50,'filled','g');
        legendText = [legendText 'Foreground'];
    end
    
end
if(numel(dataX_ibtw') > 0)
    scatter(dataX_ibtw,dataY_ibtw,50,'filled','b');
    legendText = [legendText 'Inbetween'];
end
% legendText
% numel(dataX_back)
% numel(dataX_fore)
% numel(dataX_ibtw)
hold off;
 
if(numel([dataX_back', dataX_fore', dataX_ibtw']) > 0)    
    legend(legendText,'FontSize',20);
end


if(logYaxis == 1)
    set(gca,'YScale','log');
end

allXValues = [dataX_back', dataX_fore', dataX_back'];
minX = min(allXValues(allXValues > 0));
maxX = max(allXValues);




%set axis limit
if(minX ~=maxX)
    xlim([minX, maxX])
else
    minX = minX - datenum(minutes(5));
end

dynamicDateTicks();



end

