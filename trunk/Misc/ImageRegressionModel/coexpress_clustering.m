clear all;

% stage 1 starts from index:2
% stage 1 gene list starts from index:8
% stage 1 gene list ends at index:39
% stage 2 starts from index:40
% stage 2 gene list starts from index:46
% stage 2 gene list ends at index:79
% stage 3 starts from index:80
% stage 3 gene list starts from index:86
% stage 3 gene list ends at index:131
% stage 4 starts from index:132
% stage 4 gene list starts from index:138
% stage 4 gene list ends at index:224
% stage 5 starts from index:225
% stage 5 gene list starts from index:231
% stage 5 gene list ends at index:318
% stage 6 starts from index:319
% stage 6 gene list starts from index:325
% stage 6 gene list ends at index:411
% #1:CG31607 #2:Cyp310a1 #3:D #4:Kr #5:Traf1 #6:bcd #7:cad #8:croc #9:eve #10:fkh #11:ftz #12:gt #13:h #14:hb #15:hkb #16:kni #17:knrl #18:noc #19:odd #20:prd #21:rho #22:slp1 #23:slp2 #24:sna #25:tll #26:trn #27:twi #28:zen #29:KrP #30:bcdP #31:gtP #32:hbP 
% #1:CG10924 #2:CG31607 #3:D #4:Kr #5:Traf1 #6:bcd #7:brk #8:cad #9:croc #10:eve #11:fkh #12:ftz #13:gt #14:h #15:hb #16:hkb #17:kni #18:knrl #19:noc #20:odd #21:prd #22:rho #23:slp1 #24:slp2 #25:sna #26:tll #27:trn #28:tsh #29:twi #30:zen #31:KrP #32:bcdP #33:gtP #34:hbP 
% #1:CG10924 #2:CG17786 #3:CG4702 #4:Cyp310a1 #5:D #6:Dfd #7:Doc2 #8:Kr #9:Traf1 #10:brk #11:bun #12:cad #13:cnc #14:croc #15:emc #16:eve #17:fj #18:fkh #19:ftz #20:gt #21:h #22:hb #23:hkb #24:kni #25:knrl #26:oc #27:odd #28:path #29:prd #30:rho #31:sala #32:slp1 #33:slp2 #34:sna #35:sob #36:srp #37:term #38:tll #39:trn #40:tsh #41:twi #42:zen #43:KrP #44:bcdP #45:gtP #46:hbP 
% #1:Alh #2:Ama #3:Ance #4:Blimp-1 #5:Bsg25A #6:Btk29A #7:CG10479 #8:CG10924 #9:CG11208 #10:CG14427 #11:CG17724 #12:CG17786 #13:CG31607 #14:CG31670 #15:CG8147 #16:CG8965 #17:Cyp310a1 #18:D #19:Dfd #20:Doc2 #21:Doc3 #22:Esp #23:Ilp4 #24:ImpE2 #25:Kr #26:MESR3 #27:Mdr49 #28:Mes2 #29:Nek2 #30:NetA #31:Traf1 #32:aay #33:apt #34:bmm #35:brk #36:bun #37:cad #38:chrb #39:cnc #40:comm2 #41:croc #42:dan #43:danr #44:disco #45:dpn #46:emc #47:eve #48:fj #49:fkh #50:ftz #51:gk #52:gt #53:h #54:hb #55:hkb #56:htl #57:jumu #58:ken #59:kni #60:knrl #61:mfas #62:nub #63:numb #64:oc #65:odd #66:path #67:peb #68:prd #69:pxb #70:rho #71:rib #72:sala #73:slp1 #74:slp2 #75:sna #76:sob #77:srp #78:tkv #79:tll #80:toc #81:trn #82:tsh #83:twi #84:zen #85:KrP #86:gtP #87:hbP 
% #1:Alh #2:Ama #3:Ance #4:Antp #5:Blimp-1 #6:Bsg25A #7:Btk29A #8:CG10479 #9:CG10924 #10:CG13333 #11:CG14427 #12:CG17724 #13:CG31670 #14:CG4702 #15:CG8147 #16:CG8965 #17:Cyp310a1 #18:D #19:Dfd #20:Esp #21:HLHm5 #22:Ilp4 #23:ImpE2 #24:ImpL2 #25:Kr #26:MESR3 #27:Mdr49 #28:Nek2 #29:NetA #30:Traf1 #31:aay #32:apt #33:bmm #34:brk #35:bun #36:cad #37:cenG1A #38:chrb #39:cnc #40:comm2 #41:croc #42:dan #43:danr #44:disco #45:dpn #46:edl #47:emc #48:eve #49:fj #50:fkh #51:ftz #52:gk #53:gt #54:h #55:hb #56:hkb #57:jumu #58:kni #59:knrl #60:lok #61:mfas #62:noc #63:nub #64:numb #65:oc #66:odd #67:path #68:peb #69:prd #70:pxb #71:rho #72:rib #73:slp1 #74:slp2 #75:sna #76:sob #77:srp #78:term #79:tkv #80:tll #81:toc #82:trn #83:tsh #84:twi #85:zen #86:KrP #87:gtP #88:hbP 
% #1:Ama #2:Antp #3:Blimp-1 #4:Bsg25A #5:Btk29A #6:CG10479 #7:CG11208 #8:CG13333 #9:CG14427 #10:CG17724 #11:CG17786 #12:CG31607 #13:CG31670 #14:CG8147 #15:CG8965 #16:Cyp310a1 #17:D #18:Dfd #19:Doc2 #20:Doc3 #21:Esp #22:HLHm5 #23:Ilp4 #24:ImpE2 #25:ImpL2 #26:Kr #27:MESR3 #28:Mdr49 #29:Mes2 #30:Nek2 #31:NetA #32:Traf1 #33:aay #34:apt #35:bmm #36:brk #37:bun #38:cad #39:cenG1A #40:cnc #41:comm2 #42:croc #43:dan #44:danr #45:dpn #46:edl #47:emc #48:eve #49:fj #50:fkh #51:ftz #52:gk #53:gt #54:h #55:hb #56:hkb #57:jumu #58:ken #59:kni #60:knrl #61:lok #62:mfas #63:noc #64:nub #65:numb #66:oc #67:odd #68:peb #69:prd #70:pxb #71:rho #72:sala #73:slp1 #74:slp2 #75:sna #76:sob #77:srp #78:tkv #79:tll #80:toc #81:trn #82:tsh #83:twi #84:zen #85:KrP #86:gtP #87:hbP 


s1 = char('CG31607','Cyp310a1','D','Kr','Traf1','bcd','cad','croc','eve','fkh','ftz','gt','h','hb','hkb','kni','knrl','noc','odd','prd','rho','slp1','slp2','sna','tll','trn','twi','zen','KrP','bcdP','gtP','hbP');
s2 = char('CG10924','CG31607','D','Kr','Traf1','bcd','brk','cad','croc','eve','fkh','ftz','gt','h','hb','hkb','kni','knrl','noc','odd','prd','rho','slp1','slp2','sna','tll','trn','tsh','twi','zen','KrP','bcdP','gtP','hbP');
s3 = char('CG10924','CG17786','CG4702','Cyp310a1','D','Dfd','Doc2','Kr','Traf1','brk','bun','cad','cnc','croc','emc','eve','fj','fkh','ftz','gt','h','hb','hkb','kni','knrl','oc','odd','path','prd','rho','sala','slp1','slp2','sna','sob','srp','term','tll','trn','tsh','twi','zen','KrP','bcdP','gtP','hbP');
s4 = char('Alh','Ama','Ance','Blimp-1','Bsg25A','Btk29A','CG10479','CG10924','CG11208','CG14427','CG17724','CG17786','CG31607','CG31670','CG8147','CG8965','Cyp310a1','D','Dfd','Doc2','Doc3','Esp','Ilp4','ImpE2','Kr','MESR3','Mdr49','Mes2','Nek2','NetA','Traf1','aay','apt','bmm','brk','bun','cad','chrb','cnc','comm2','croc','dan','danr','disco','dpn','emc','eve','fj','fkh','ftz','gk','gt','h','hb','hkb','htl','jumu','ken','kni','knrl','mfas','nub','numb','oc','odd','path','peb','prd','pxb','rho','rib','sala','slp1','slp2','sna','sob','srp','tkv','tll','toc','trn','tsh','twi','zen','KrP','gtP','hbP');
s5 = char('Alh','Ama','Ance','Antp','Blimp-1','Bsg25A','Btk29A','CG10479','CG10924','CG13333','CG14427','CG17724','CG31670','CG4702','CG8147','CG8965','Cyp310a1','D','Dfd','Esp','HLHm5','Ilp4','ImpE2','ImpL2','Kr','MESR3','Mdr49','Nek2','NetA','Traf1','aay','apt','bmm','brk','bun','cad','cenG1A','chrb','cnc','comm2','croc','dan','danr','disco','dpn','edl','emc','eve','fj','fkh','ftz','gk','gt','h','hb','hkb','jumu','kni','knrl','lok','mfas','noc','nub','numb','oc','odd','path','peb','prd','pxb','rho','rib','slp1','slp2','sna','sob','srp','term','tkv','tll','toc','trn','tsh','twi','zen','KrP','gtP','hbP');
s6 = char('Ama','Antp','Blimp-1','Bsg25A','Btk29A','CG10479','CG11208','CG13333','CG14427','CG17724','CG17786','CG31607','CG31670','CG8147','CG8965','Cyp310a1','D','Dfd','Doc2','Doc3','Esp','HLHm5','Ilp4','ImpE2','ImpL2','Kr','MESR3','Mdr49','Mes2','Nek2','NetA','Traf1','aay','apt','bmm','brk','bun','cad','cenG1A','cnc','comm2','croc','dan','danr','dpn','edl','emc','eve','fj','fkh','ftz','gk','gt','h','hb','hkb','jumu','ken','kni','knrl','lok','mfas','noc','nub','numb','oc','odd','peb','prd','pxb','rho','sala','slp1','slp2','sna','sob','srp','tkv','tll','toc','trn','tsh','twi','zen','KrP','gtP','hbP');


%stage1
load virtualembryo;
s1_co =data(:,2:4);
%stage1 = data(:,8:39);
stage1 = data(:,8:35); % remove the last 4 protein expressions. 'KrP','bcdP','gtP','hbP'
s1_labels = char('CG31607','Cyp310a1','D','Kr','Traf1','bcd','cad','croc','eve','fkh','ftz','gt','h','hb','hkb','kni','knrl','noc','odd','prd','rho','slp1','slp2','sna','tll','trn','twi','zen');
Z = linkage(stage1', 'ward','euclidean')
H  = dendrogram(Z, 0, 'orientation', 'right', 'labels',s1_labels, 'colorThreshold', 35)
set(H,'LineWidth',2)

% stage 2
s2_co =data(:,40:42);
s2_labels = char('CG10924','CG31607','D','Kr','Traf1','bcd','brk','cad','croc','eve','fkh','ftz','gt','h','hb','hkb','kni','knrl','noc','odd','prd','rho','slp1','slp2','sna','tll','trn','tsh','twi','zen');
%stage2 = data(:,46:79);
stage2 = data(:,46:75);
Z = linkage(stage2', 'ward','euclidean')
H  = dendrogram(Z, 0, 'orientation', 'right', 'labels',s2_labels,'colorThreshold', 35)


% stage 3
s3_co =data(:,80:32);
s3_labels = char('CG10924','CG17786','CG4702','Cyp310a1','D','Dfd','Doc2','Kr','Traf1','brk','bun','cad','cnc','croc','emc','eve','fj','fkh','ftz','gt','h','hb','hkb','kni','knrl','oc','odd','path','prd','rho','sala','slp1','slp2','sna','sob','srp','term','tll','trn','tsh','twi','zen');
%stage3 = data(:,86:131);
stage3 = data(:,86:127);
Z = linkage(stage3', 'ward','euclidean')
H  = dendrogram(Z, 0, 'orientation', 'right', 'labels',s3_labels, 'colorThreshold', 35)

%stage 4
s4_co = data(:,132:134);
stage4 = data(:,138:224);
Z = linkage(stage4', 'ward','euclidean')
H  = dendrogram(Z, 0, 'orientation', 'right', 'labels',s4,'colorThreshold', 35)

%stage 5
s5_co = data(:,225:227);
stage5 = data(:,231:318);
Z = linkage(stage5', 'ward','euclidean')
H  = dendrogram(Z, 0, 'orientation', 'right', 'labels',s5,'colorThreshold', 35)


%stage 6
s6_co = data(:,319:321);
s6_labels = char('Ama','Antp','Blimp-1','Bsg25A','Btk29A','CG10479','CG11208','CG13333','CG14427','CG17724','CG17786','CG31607','CG31670','CG8147','CG8965','Cyp310a1','D','Dfd','Doc2','Doc3','Esp','HLHm5','Ilp4','ImpE2','ImpL2','Kr','MESR3','Mdr49','Mes2','Nek2','NetA','Traf1','aay','apt','bmm','brk','bun','cad','cenG1A','cnc','comm2','croc','dan','danr','dpn','edl','emc','eve','fj','fkh','ftz','gk','gt','h','hb','hkb','jumu','ken','kni','knrl','lok','mfas','noc','nub','numb','oc','odd','peb','prd','pxb','rho','sala','slp1','slp2','sna','sob','srp','tkv','tll','toc','trn','tsh','twi','zen');
%stage6 = data(:,325:411);
stage6 = data(:,325:408);  %last 3 are protein expression
Z = linkage(stage6', 'ward','euclidean')
H  = dendrogram(Z, 0, 'orientation', 'right', 'labels',s6_labels,'colorThreshold', 35)
set(H,'LineWidth',2)



