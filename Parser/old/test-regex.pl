

my $t = "salut";
my $t2 = "com.epfl.appspy";

my $exp = "(([a-zA-Z]+){1}(\.[a-zA-Z]+)+)"; #find word.separated.by.point.


if($t =~ /\Q$exp/g){
	print "trouvé\n";
	print $1;
}
else {
	print "pas trouvé";
}

print "\n";

if($t2 =~ /$exp/g){
	print "trouvé\n";
	print $1;
}
else {
	print "pas trouvé";
}

#print $t;





