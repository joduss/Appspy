

def toBool(val):
	#print(val)

	if(str(val) == "True"):
		return True
	elif(str(val) == "False"):
		return False
	else:
		raise NameError('Error toBool')