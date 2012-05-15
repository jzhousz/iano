
clear all;

%51: ftz %58:ken;  48: eve hb: 55;   gt: 53   %bcd: 
%EVE: 48
%HBp: 87  (activator for stripe 2)
%gtp: 86  (repressor for stripe 2)
%krp: 85  (repressor for stripe 2)
% restricting CRM output to a narrow stripe lying between the single band of KR expression and the anterior expression domain of GT

eve = unrolltomap(53);
input = unrolltomap(87);

% needed? Since eve has only one channel?
Y =double( .2989*eve(:,:,1)+.5870*eve(:,:,2)+.1140*eve(:,:,3));
X =double( .2989*input(:,:,1)+.5870*input(:,:,2)+.1140*input(:,:,3));

% call ex1v3
[bhat, ahat, yhat] = imgPointwiseReg(Y, X, 2, 4)

% bhat outside of stripes  are not our interest for biological interpretation, so make them 0.
BTH = 0.35;  %BINARIZATION OF EVE PATTERN. The bigger the numebr, the narrower the stripe outline.
twodeve = eve(:,:,1);
blk = find(twodeve <= BTH);
cleanedbhat = bhat;
cleanedbhat(blk) = 0;
figure; surf(bhat);shading flat;view(-180,90);
figure; surf(cleanedbhat);shading flat;view(-180,90);
axis([0 360  0 180])

% add outline to highlight stripes
twodeve = eve(:,:,1);
blk = find(twodeve <= BTH);
white = find(twodeve > BTH);
test = twodeve;
test(blk) = 0;
test(white) = 255;
%imshow(test);
BWoutline = bwperim(test);
Segout = cleanedbhat;
Segout(BWoutline) = 4.; %set to an outstanding color

% export figure
figure; surf(Segout);shading flat;view(180,90);
axis([0 360  0 180]);
set(gca,'XTick',0);
set(gca,'YTick',0);
set(gca,'ZTick',0);
colorbar;
exportfig(gcf,'test.jpg','Format','jpeg', 'color', 'cmyk');

%figure, imshow(Segout), title('outlined original image');



%%
% image(yhat/max(yhat(:))*100);
%figure;surf(ahat);shading flat;
%figure;image(bhat);

% plot predicted expression in red channel
py =  (yhat-min(yhat(:)))/(max(yhat(:))-min(yhat(:))); %0-1
pyimg = zeros(size(Y,1), size(Y,2), 3);
pyimg(:,:,1) = py;
figure; image(pyimg);

%residue
%figure; surf(eve(:,:,1)-py); %? why not y - py? because 0-1?
%shading flat;

%R square
resid = eve(:,:,1)-py;
Y_0 = eve(:,:,1);
R_sq = 1 -sum(sum(resid(:).^2))/sum(sum((Y_0-mean(Y_0(:))).^2))