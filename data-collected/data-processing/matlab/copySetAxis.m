function[] = copySetAxis(axToCopy, axToSet, logYaxis)
if(logYaxis == 1)
    set(gca,'YScale','log');
    ylim([10^(-2) 10^6]);
else
    ylim([0 10^5]);
end

%set x ticks
newTicks = linspace(1,max(axToSet.XTick), numel(axToCopy.XTick));
axToSet.XTick = newTicks;
axToSet.XTickLabel = axToCopy.XTickLabel;

end