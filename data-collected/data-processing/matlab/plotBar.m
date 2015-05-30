function [ fig ] = plotBar(  dataX_back, dataY_back, dataX_fore, dataY_fore, dataX_ibtw, dataY_ibtw,...
    showParam, logYaxis, visible)
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here
fig = figure('units','normalized','outerposition',[0 0 1 1],'visible',visible);

dataX = [];
legendText = {};
stackedDataY = [];
contained=[0 0 0];

if(strcmp(showParam,'b') || strcmp(showParam,'a'))
    %if(numel(dataX_back) > 0)
        legendText = [legendText 'Background'];
%         stackedDataY = dataY_back;
        dataX = union(dataX, dataX_back);
        contained(1)=1;
    %end    
end
if(strcmp(showParam,'f') || strcmp(showParam,'a'))
    %if(numel(dataX_fore) > 0)       
        legendText = [legendText 'Foreground'];
%         stackedDataY = [dataY_back; dataY_fore];
        dataX = union(dataX, dataX_fore);
        contained(2) = 1;
    %end
end
%if(numel(dataX_ibtw') > 0)
    legendText = [legendText 'Inbetween'];
%     stackedDataY = [stackedDataY; dataY_ibtw];
    dataX = union(dataX, dataX_ibtw);
    contained(3) = 1;
%end

dataX = unique(dataX,'sorted');

stackedDataY = zeros(numel(legendText),numel(dataX));

for idx = 1 : numel(dataY_back)
    idxToAdd = find(dataX ==dataX_back(idx));
    stackedDataY(1,idxToAdd) = dataY_back(idx);
end

for idx = 1 : numel(dataY_fore)
    idxToAdd = find(dataX ==dataX_fore(idx));
    stackedDataY(sum(contained(1:2)),idxToAdd) = dataY_fore(idx);
end

for idx = 1 : numel(dataY_ibtw)
    idxToAdd = find(dataX ==dataX_ibtw(idx)); 
    stackedDataY(sum(contained(1:3)),idxToAdd) = dataY_ibtw(idx);
end


padded = 0;
if(numel(dataX) == 1)
    dataX = [0 dataX];
    stackedDataY = [[0;0;0] stackedDataY];
    padded=1;
end
B = bar(dataX, stackedDataY','stacked');

color={[0 0 1],[0.4 1 0.4],[1 0 0]};

%  if(numel(dataX) > 1)
%      minT = min(dataX) 
%      set(gca,'Xtick',linspace(minT, max(dataX),40))
%  end
% datetick('x','mm yyyy','keeplimits', 'keepticks')
if(padded == 1)
    %need to focus the axis. No need to show the 0 padded.
    maxX = max(dataX) + datenum(minutes(1));
   xlim([maxX - datenum(minutes(1)), maxX]); 
end

dynamicDateTicks();

ax=gca;
ax.XTickLabelRotation = 45;

for i = 1:numel(B)
   set(B(i),'facecolor', color{i}); 
end

if(numel([dataX_back(:)', dataX_fore(:)', dataX_ibtw(:)']) > 0)    
    legend(legendText,'FontSize',20);
end


if(logYaxis == 1)
    set(gca,'YScale','log');
end


end

