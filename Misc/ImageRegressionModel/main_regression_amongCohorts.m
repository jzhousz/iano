%%
%%
% 5/1/2012
% 
% Dynamic model
% Experiments among time cohorts to evaluate the model
% 
% use previous stage to predict stage 6
% 1. What about regulation among genes?  Still possible to use it? 
% 2. Should  I use protein or gene expresson?

% Distributio of data in the atlas:
%     case 1
%         co = data(:,2:4);
%         expression = data(:,8:39);
%     case 2
%         co =  data(:,40:42);  %AN, AO, AP
%         expression = data(:,43:79);  %AQ to CA
%     case 3
%         co =   data(:,80:82);  % CB, CC, CD
%         expression = data(:,83:131);  % CE, to EA
%     case 4
%         co =     data(:,132:134);       % EB, EC, ED
%         expression =    data(:,135:224);  % EE to HP
%     case 5                 
%         co =    data(:, 225:227); % HQ HR HS
%         expression =  data(:, 228:318);  % HT to LF
%     case 6
%         co = data(:,319:321);   %LG LH LI
%         expression = data(:,325:411);   %LJ -OU
% %
clear all;


%  Need to find a gene that appears at all stages ...
%  Should I use gene expression or protein expression or mRNA?
%  Conclusion for now:  Use HBp (Protein expression of HB)
%    s6: OU    87
%    s1: AM  39 -> 39-8+1 = 32
%    s2: CA  79 -> 79-43+1 = 37
%    s3: EA  131 -> 131-83+1 = 49
%    s4: HP  224 -> 224-135+1 = 90
%    s5: LF  318 -> 318-228+1 = 91

target = unrolltomap(87, 6);
input1 = unrolltomap(32, 1);
input2 = unrolltomap(37, 2);
input3 = unrolltomap(49, 3);
input4 = unrolltomap(90, 4);
input5 = unrolltomap(91, 5);

Y =double( .2989*target(:,:,1)+.5870*target(:,:,2)+.1140*target(:,:,3));
X1 =double( .2989*input1(:,:,1)+.5870*input1(:,:,2)+.1140*input1(:,:,3));
X2 =double( .2989*input1(:,:,1)+.5870*input1(:,:,2)+.1140*input1(:,:,3));
%X3 =double( .2989*input1(:,:,1)+.5870*input1(:,:,2)+.1140*input1(:,:,3));
%X4 =double( .2989*input1(:,:,1)+.5870*input1(:,:,2)+.1140*input1(:,:,3));
%X5 =double( .2989*input1(:,:,1)+.5870*input1(:,:,2)+.1140*input1(:,:,3));

%X = [X1 X2 X3 X4 X5]  %180*1800

X(:,:,1) = X1;
X(:,:,2) = X2;

% call ex1v3

% how to input multiple X????
[bhat, ahat, yhat] = imgPointwiseReg(Y, X, 2, 4)


% visualize results

% bhat outside of stripes  are not our interest for biological interpretation, so make them 0.
BTH = 0.30;  %BINARIZATION OF EVE (Target) PATTERN. The bigger the numebr, the narrower the stripe outline.

twodeve = target(:,:,1);
blk = find(twodeve <= BTH);
cleanedbhat = bhat;
cleanedbhat(blk) = 0;
figure; surf(bhat);shading flat;view(-180,90);
figure; surf(cleanedbhat);shading flat;view(-180,90);
axis([0 360  0 180])

% add outline to highlight stripes
twodeve = target(:,:,1);
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

% ploy predicted expression in red channel
py =  (yhat-min(yhat(:)))/(max(yhat(:))-min(yhat(:))); %0-1
pyimg = zeros(size(Y,1), size(Y,2), 3);
pyimg(:,:,1) = py;
figure; image(pyimg);

