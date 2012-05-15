clear all;

%stage1
load virtualembryo;
stage1 = data(:,8:39);
s = char('CG31607',	'Cyp310a1',	'D',	'Kr',	'Traf1',	'bcd',	'cad',	'croc',	'eve',	'fkh',	'ftz',	'gt',	'h',	'hb',	'hkb',	'kni',	'knrl',	'noc',	'odd',	'prd',	'rho',	'slp1',	'slp2','sna',	'tll',	'trn',	'twi',	'zen',	'KrP', 'bcdP',	'gtP',	'hbP');

%Y = pdist(stage1','euclidean');
%Z = linkage(Y, 'complete','euclidean');
Z = linkage(stage1', 'ward','euclidean')

H  = dendrogram(Z, 0, 'orientation', 'right', 'labels',s, 'colorThreshold', 35)
set(H,'LineWidth',2)


