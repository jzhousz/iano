CC = g++
DEBUG_FLAG =         # assign -g for debugging

OBJS = mrmr.o 
#OBJS = mrmr.o /home/hpeng/work/3rdsoft/libmba-0.9.1/src/msgno.o csv_phc.o 

mrmr : ${OBJS}
	${CC} ${DEBUG_FLAG} ${OBJS} -o $@

mrmr.o : mrmr.cpp
	${CC} -c mrmr.cpp

#csv_phc.o : csv_phc.c csv_phc.h
#	${CC} -c csv_phc.c

clean :
	rm mrmr.o
#	rm estmutualinfo.o csv_phc.o

