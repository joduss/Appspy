function [ packageNames ] = allPackageName( database )
%UNTITLED3 Summary of this function goes here
%   Detailed explanation goes here
    results = sqlite3.execute(database, 'SELECT DISTINCT package_name from table_installed_apps');
    packageNames = {results.package_name};

end

