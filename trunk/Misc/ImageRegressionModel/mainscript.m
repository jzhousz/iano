hb = imread('hb_s5_6.png');
eve =imread('eve_s5_6.png');
%cut the size
hb_same = hb(1:385, 1:493, :);
%change to gray scale
Y =double( .2989*eve(:,:,1)+.5870*eve(:,:,2)+.1140*eve(:,:,3));
X =double( .2989*hb_same(:,:,1)+.5870*hb_same(:,:,2)+.1140*hb_same(:,:,3));

% call ex1v3
[bhat, ahat, yhat] = imgPointwiseReg(Y, X, 4, 5)

image(yhat/max(yhat(:))*100);
figure; surf(bhat);shading flat;
figure;surf(ahat);shading flat;

% ploy predicted expression in red channel
py =  (yhat-min(yhat(:)))/(max(yhat(:))-min(yhat(:))); %0-1
pyimg = zeros(size(Y,1), size(Y,2), 3);
pyimg(:,:,1) = py;
image(pyimg);


% image(X_orig/max(X_orig)*100);
% image(X/max(X)*100);
% X =double( .2989*hb_same(:,:,1)+.5870*hb_same(:,:,2)+.1140*hb_same(:,:,3));
% image(X/max(X)*100);
% image(X);
% image(X_orig);


