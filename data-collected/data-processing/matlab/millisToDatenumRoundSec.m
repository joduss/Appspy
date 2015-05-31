function [ dn ] = millisToDatenumRoundSec( millis )
%MILLISTODATENUM 
%Convert unix millisecond time to matlab datenum. At the same time,
%round to second = 0
timestamp = (millis+2*60*60000)/1000;
time = [1970 1 1 0 0 timestamp];
t = datetime(time,'InputFormat','dd-MMM-yyyy HH:mm:ss');
t.Second = 0; %round time as record may have + 1 to 10 seconds of delay
dn = datenum(t);

end

