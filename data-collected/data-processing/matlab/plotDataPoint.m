function [fig ] = plotDataPoint( dataX_back, dataY_back, dataX_fore, dataY_fore, dataX_ibtw, dataY_ibtw,...
    showParam, logYaxis, visible, fsize)
%UNTITLED3 Summary of this function goes here
%   Detailed explanation goes here

if (~exist('fsize', 'var'))
    fsize = 24;
end

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
    scatter(dataX_ibtw,dataY_ibtw,70,'filled','b');
    legendText = [legendText 'Inbetween'];
end
% legendText
% numel(dataX_back)
% numel(dataX_fore)
% numel(dataX_ibtw)
hold off;
 
if(numel([dataX_back(:)', dataX_fore(:)', dataX_ibtw(:)']) > 0)    
    l = legend(legendText,'FontSize',20);
    set(l,'FontSize',fsize);
end


if(logYaxis == 1)
    set(gca,'YScale','log');
end

allXValues = [dataX_back(:)', dataX_fore(:)', dataX_back(:)'];
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

