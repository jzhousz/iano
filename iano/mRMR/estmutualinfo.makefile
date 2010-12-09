CC = g++
DEBUG_FLAG =         # assign -g for debugging

OBJS = estmutualinfo.o 
#OBJS = estmutualinfo.o /home/hpeng/work/3rdsoft/libmba-0.9.1/src/msgno.o csv_phc.o 

mutualinfo : ${OBJS}
	${CC} ${DEBUG_FLAG} ${OBJS} -o $@

estmutualinfo.o : estmutualinfo.cpp
	${CC} -c estmutualinfo.cpp

#csv_phc.o : csv_phc.c csv_phc.h
#	${CC} -c csv_phc.c

clean :
	rm estmutualinfo.o
#	rm estmutualinfo.o csv_phc.o

