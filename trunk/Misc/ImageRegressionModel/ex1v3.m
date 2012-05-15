%load -mat ../Phoenix2.mat
tic
dimx=385;
dimy=493;

%we get true_mask from slc off image
% tempimg=slc_off_landsat7(1:dimx,1:dimy);
true_mask=ones(dimx,dimy);
% true_mask(tempimg==0)=0;
% 
% Y=double(img05242001(401:400+dimx,401:400+dimy));
% X=double(img05212000(401:400+dimx,401:400+dimy));

Y =double( .2989*eve(:,:,1)+.5870*eve(:,:,2)+.1140*eve(:,:,3));
X =double( .2989*hb_same(:,:,1)+.5870*hb_same(:,:,2)+.1140*hb_same(:,:,3));

%Normalized to [0,2]
mx=mean(X(:));
sdx=std(X(:));
X=(X-mx)./sdx; %+1.00;
X_orig=X;
X(true_mask==0)=0;

J=ones(dimx,dimy);
J(true_mask==0)=0;

%Normalization to [-1,1]
my=mean(Y(:));
sdy=std(Y(:));
Y=(Y-my)./sdy;
Y_orig=Y;
Y(true_mask==0)=0;

%these are two other nobs
wname='Haar';
% tolevel=5;
% fromlevel=2;
tolevel=6;
fromlevel=5; %3;

% Let's do a test :)
useSparse = 1;
padopt=3;

a_fromlevel=fromlevel;
a_tolevel=tolevel;
b_fromlevel=fromlevel;
b_tolevel=tolevel;
rel_tol = 1.0e-6;
threshould = 0.995;

%percentage of original image size
area_patch=0.02;% 0.02 0.03 0.04];
%we will pick 5 patches
n=10; %5

%fixed random state
%state = 122256;
state = 458772;
%state = 898437;
%state = 988436;
%state = 209478;
rand('state', state);
%randn('state', state);
%we don't want wrap around
dx1=round(sqrt(area_patch)*dimx);
dy1=round(sqrt(area_patch)*dimy);
upperleftx=zeros(n,1);
upperlefty=zeros(n,1);
lowerrightx=zeros(n,1);
lowerrighty=zeros(n,1);
for j=1:n
    while 1
        centerx(j)=round(rand(1,1)*(dimx-dx1))+ceil(dx1/2);
        centery(j)=round(rand(1,1)*(dimy-dy1))+ceil(dy1/2);
        upperleftx(j)=(centerx(j)-round(dx1/2));
        upperlefty(j)=(centery(j)-round(dy1/2));
        lowerrightx(j)=(centerx(j)+round(dx1/2));
        lowerrighty(j)=(centery(j)+round(dy1/2));
        valid=sum(sum(true_mask(upperleftx(j):lowerrightx(j), upperlefty(j):lowerrighty(j))));
        if valid > 0.49*dx1*dy1
            break;
        end
    end
end

opt_lambdas=zeros(1,n);
s2s=zeros(1,n);
for j=1:n;
    sizex=lowerrightx(j)-upperleftx(j)+1;
    sizey=lowerrighty(j)-upperlefty(j)+1;
    Jsmall=J(upperleftx(j):lowerrightx(j), upperlefty(j):lowerrighty(j));
    [A01, basic_info]=getWaveletA(Jsmall,...
        wname,sizex,sizey,a_fromlevel,a_tolevel,useSparse,padopt);
    Xsmall=X(upperleftx(j):lowerrightx(j), upperlefty(j):lowerrighty(j));
    [A02, basic_info]=getWaveletA(Xsmall,...
        wname,sizex,sizey,b_fromlevel,b_tolevel,useSparse,padopt);
    A0=[A01 A02];

    Ysmall=Y(upperleftx(j):lowerrightx(j), upperlefty(j):lowerrighty(j));
    Ypatch=padding(Ysmall,...
        basic_info,b_tolevel,padopt);
    y = reshape(Ypatch,size(Ypatch,1)*size(Ypatch,2),1);   % measurements with no noise

    dx=9;
    dy=9;
    gains=zeros(sizex,sizey);
    biases=zeros(sizex,sizey);
    Y_local=zeros(sizex,sizey);
    resid_local=zeros(sizex,sizey);
    %maximal gain for a pixel value
    max_G=3;
    for i1=1:sizex
        for j1=1:sizey
            %Jsmall is the missing mask of Y
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            if Jsmall(i1,j1) == 0 %missing Y at (i1,j1)
                resid_local(i1,j1)=NaN;
                continue;
            end
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            if i1 < dx/2
                A1=1;A2=dx;
            elseif i1 > round(sizex-dx/2)
                A1=sizex-dx+1;A2=sizex;
            else
                A1=round(i1-dx/2);A2=round(i1+dx/2)-1;
            end
            if j1 < dx/2
                B1=1;B2=dy;
            elseif j1 > round(sizey-dy/2)
                B1=sizey-dy+1;B2=sizey;
            else
                B1=round(j1-dy/2);B2=round(j1+dy/2)-1;
            end
            window_slc_on=Xsmall(A1:A2,B1:B2);
            window_slc_off=Ysmall(A1:A2,B1:B2);
            window_slc_on_nonzero=reshape(window_slc_on,dx*dy,1);
            window_slc_off_nonzero=reshape(window_slc_off,dx*dy,1);
            len=length(window_slc_off_nonzero);
            Ax=[ones(len,1) window_slc_on_nonzero];
            b=regress(window_slc_off_nonzero,Ax);
            if b(2)>1/max_G && b(2) < max_G
                Gw=b(2);
                Bw=b(1);
            else
                Xbar=mean(window_slc_on(:));
                Sx=std(window_slc_on(:));

                %window_nonzero=window_slc_off(window_slc_off~=0&window_slc_off~=1);
                Ybar=mean(window_slc_off_nonzero(:));
                Sy=std(window_slc_off_nonzero(:));

                Gw=Sy/Sx;
                Bw=Ybar-Gw*Xbar;
                if Gw<=1/max_G||Gw>=max_G
                    Gw=1;
                    Bw=Ybar-Xbar;
                end
            end
            gains(i1,j1)=Gw;
            biases(i1,j1)=Bw;
            Y_local(i1,j1)=Xsmall(i1,j1)*Gw+Bw;
            resid_local(i1,j1)=Ysmall(i1,j1)-Y_local(i1,j1);
        end
    end
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    nonmissingresid=resid_local(~isnan(resid_local));
    s2=1/(length(nonmissingresid))*(sum(nonmissingresid.^2));
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    s2s(j)=s2;

    max_lambda = find_lambdamax_l1_ls(A0',y);
    if max_lambda > 20
        lambda=max_lambda/10; %/10;
    else
        lambda=max_lambda*0.9;
    end
    lambda_step=lambda/25; %/25
    i=1;
    param_num=[];
    BIC=[];
    lambdas=[];
    while lambda > 1e-2
        disp(sprintf('\nSeeking lambda area size=%.5e Area No. j=%d, cycle i=%d, lambda=%.5e',...
            area_patch,j,i,lambda));
        quiet=true(1);
        x1=coordinate_descent_adpt_lasso(A0,y,1,lambda,rel_tol);
        r=y-A0*x1; %quick way to get resid, in future I can delete above
        %v1=A0'*r;
        %v=v1/norm(v1,inf);
        %these are the index of var's being selected
        %index=(abs(v)>threshould);
        thr1=norm(x1,1)*1e-6;
        index=(abs(x1)>thr1);
        lambdas(i)=lambda;
        param_num(i)=sum(index); %estimated num of nonzero parameters
        sample_num=size(A0,1);
        RSS=r'*r;
        %AIC(i)=(RSS/s2+2*param_num(i))/sample_num;
        BIC(i)=(RSS/s2+param_num(i)*log(sample_num))/sample_num;
        [status,lambda_opt, BICScore]=findLambda(lambdas, BIC, i);
        if lambda_opt > 0
            opt_lambdas(j)=lambda_opt;
        end
        if status == 1
            break;
        end
        i=i+1;
        if lambda > 1.1*lambda_step
            lambda=lambda-lambda_step;
        else
            lambda=lambda/5;
        end
    end
    if status == 0
        opt_lambdas(j)=1e-3;
    end
end

disp('seeking ended.');
final_lambda=mean(opt_lambdas); 

images=ones(dimx,dimy,2);
images(:,:,2)=X;
tic;
[A0, final_dimx, final_dimy, basic_info]= ...
    getWaveletDesignMat(images,wname, ...
    dimx,dimy,a_fromlevel,a_tolevel,useSparse,padopt);

Ypatch=padding(Y,...
    basic_info,b_tolevel,padopt);
y  = reshape(Ypatch,size(Ypatch,1)*size(Ypatch,2),1);

rel_tol = 1.0e-6;     % relative target duality gap
%final_lambda=15.0
x1=coordinate_descent_adpt_lasso(A0,y,1,final_lambda,rel_tol);
%x1=l1_ls(A0,y,final_lambda,rel_tol);
len=size(x1,1);

ahat=recon(x1(1:len/2),basic_info,a_fromlevel,a_tolevel,dimx,dimy);
%         surf(ahat);shading flat;
%         image(ahat*100)

bhat=recon(x1(len/2+1:len),basic_info,b_fromlevel,b_tolevel,dimx,dimy);
%         surf(bhat);shading flat;
%         image(bhat*100)

%yhat1 = ahat.*J+bhat.*X;
%         surf(yhat1);

yhat = ahat+bhat.*X_orig;
%         surf(yhat);
%         shading flat;

%yhat2=yhat.*(~true_mask)+Y.*(true_mask);
image(yhat/max(yhat(:))*100);
image(X_orig/max(X_orig)*100);
ttt1=((yhat2)-min(yhat2(:))) ...
    /(max(yhat2(:))-min(yhat2(:)));

maskborder=ones(dimx,dimy);
mask=~true_mask;
for i=1:dimx
    for j=1:dimy
        if mask(i,j)==0
            if(i+1)<=dimx & mask(i+1,j)==1
                maskborder(i,j)=0;
                continue;
            end
            if(j+1)<=dimy & mask(i,j+1)==1
                maskborder(i,j)=0;
                continue;
            end
            if(i-1)>=1 & mask(i-1,j)==1
                maskborder(i,j)=0;
                continue;
            end
            if(j-1)>=1 & mask(i,j-1)==1
                maskborder(i,j)=0;
                continue;
            end
        end
    end
end
figure;image(ttt1.*maskborder*100);

figure; surf(yhat-Y_orig); shading flat;
norm(yhat-Y_orig,2)/norm(Y_orig,2)
imshow(uint8(Y_orig*sdy+my))
Y_0=double(img05242001(401:400+dimx,401:400+dimy));
resid=(yhat*sdy+my)-Y_0;
hist(resid(:),100)
R_sq=1-sum(sum(resid(:).^2))/sum(sum((Y_0-mean(Y_0(:))).^2))
resid_clm=resid(true_mask==0);
figure;hist((resid_clm),200);
surf(resid); shading flat;
imshow(uint8(yhat*sdy+my))

temp1=Y_0(true_mask==0);
temp1=temp1-mean(temp1);
R_sq_gap=1-sum(resid_clm.^2)/(sum(temp1.^2))
t=toc

%residual bootstrapping to obtain variance
yhat_visible=yhat;
yhat_visible(true_mask==0)=0;

resid2=Y-yhat;
resid2(true_mask==0)=0;
rmean=mean(resid2(:));
resid2=resid2-rmean;
[u d v]=svd(resid2,'econ');

sample_sz=100;
newd=zeros(min(dimx,dimy),sample_sz); 
dorig=diag(d);
resid2inv=v*diag(1./dorig)*u';

% for i = 1:sample_sz
%   d1=zeros(min(dimx,dimy),1);
%   for k=1:min(dimx,dimy)
%      dd1=randperm(min(dimx,dimy));
%      d1(k)=dorig(dd1(1))*(1+rand()/10);
%   end
% newd(:,i)=sort(d1,'descend');
% end

newresid2=zeros(dimx,dimy,sample_sz);
dist2=zeros(sample_sz,1);
for i = 1:sample_sz
%dnew=diag(newd(:,i));
rd1=floor(rand(dimx*dimy,1)*(dimx*dimy)+1);
newresid2inv=reshape(resid2inv(rd1),dimy,dimx);
newresid2(:,:,i)=resid2*newresid2inv*resid2; %G-inv: B=B*B^-1*B %u*dnew*v';
dist2(i)=sqrt(sum(sum((newresid2(:,:,i)-resid2).^2)));
end
hist(dist2,15)

dist3=zeros((sample_sz-1)*sample_sz/2,1);
cur=1;
for i = 1:sample_sz-1
for j = i+1:sample_sz
  dist3(cur)=sqrt(sum(sum((newresid2(:,:,i)-newresid2(:,:,j)).^2)));
  cur=cur+1;
end
end
figure;hist(dist3,15)

t1=sum(A0.^2); t3=A0'*A0;
yhats=zeros(dimx,dimy,sample_sz);
ahats=zeros(dimx,dimy,sample_sz);
bhats=zeros(dimx,dimy,sample_sz);
x2s=zeros(length(x1),sample_sz);
rel_tol = 1.0e-6;     % relative target duality gap
for i=1:sample_sz
fprintf(1,'sample %d \n',i);
new_Y=yhat_visible+newresid2(:,:,i)+rmean; %don't add rmean for now.
new_Y(true_mask==0)=0;

Ypatch=padding(new_Y,...
    basic_info,b_tolevel,padopt);
y  = reshape(Ypatch,size(Ypatch,1)*size(Ypatch,2),1);
t2=A0'*y; 

x2s(:,i)=coordinate_descent_adpt_lasso(A0,y,1,final_lambda,rel_tol,t1,t2,t3);
%x1=l1_ls(A0,y,final_lambda,rel_tol);
len=size(x1,1);

ahats(:,:,i)=recon(x2s(1:len/2,i),basic_info,a_fromlevel,a_tolevel,dimx,dimy);
%         surf(ahat);shading flat;
%         image(ahat*100)

bhats(:,:,i)=recon(x2s(len/2+1:len,i),basic_info,b_fromlevel,b_tolevel,dimx,dimy);
%         surf(bhat);shading flat;
%         image(bhat*100)

%yhat1 = ahat.*J+bhat.*X;
%         surf(yhat1);

yhats(:,:,i) = ahats(:,:,i)+bhats(:,:,i).*X_orig;
%         surf(yhat);
%         shading flat;
end

sd1=zeros(dimx,dimy);
for i=1:dimx
    for j=1:dimy
        temp=yhats(i,j,:);
        sd1(i,j)=std(temp(:));
    end
end
contourf(sd1,10)
surf(sd1);shading flat

qt1=zeros(dimx,dimy);
qt2=zeros(dimx,dimy);
for i=1:dimx
    for j=1:dimy
        temp=yhats(i,j,:);
        qt1(i,j)=quantile(temp(:),0.025);
        qt2(i,j)=quantile(temp(:),0.975);
    end
end
surf(qt1);shading flat
surf(qt2);shading flat
surf(qt2-qt1);shading flat;
xt1=zeros(dimx,dimy);
xt1((yhat)>qt1&(yhat)<qt2)=1;
figure;surf(xt1);shading flat;

j=260  %2,100,260, 1-260
figure;plot(yhat(:,j));hold on;plot(qt2(:,j),'r');plot(qt1(:,j),'g');
figure;plot3(1:200,repmat(j,dimx),yhat(:,j),...
    1:200,repmat(j,dimx),qt2(:,j),'r',...
    1:200,repmat(j,dimx),qt1(:,j),'g'); hold on
j=100
plot3(1:200,repmat(j,dimx),yhat(:,j),...
    1:200,repmat(j,dimx),qt2(:,j),'r',...
    1:200,repmat(j,dimx),qt1(:,j),'g'); grid on
j=200
plot3(1:200,repmat(j,dimx),yhat(:,j),...
    1:200,repmat(j,dimx),qt2(:,j),'r',...
    1:200,repmat(j,dimx),qt1(:,j),'g'); grid on

j=2  %1-200
figure;plot(yhat(j,:));hold on;plot(qt2(j,:),'r');plot(qt1(j,:),'g');

qtb1=zeros(dimx,dimy);
qtb2=zeros(dimx,dimy);
for i=1:dimx
    for j=1:dimy
        temp=bhats(i,j,:);
        qtb1(i,j)=quantile(temp(:),0.025);
        qtb2(i,j)=quantile(temp(:),0.975);
    end
end
surf(qtb2-qtb1);shading flat;
xtb1=zeros(dimx,dimy);
xtb1((bhat)>qtb1&(bhat)<qtb2)=1;
figure;surf(xtb1);shading flat;

qtb1=zeros(dimx,dimy);
qtb2=zeros(dimx,dimy);
for i=1:dimx
    for j=1:dimy
        temp=ahats(i,j,:);
        qtb1(i,j)=quantile(temp(:),0.025);
        qtb2(i,j)=quantile(temp(:),0.975);
    end
end
surf(qtb2-qtb1);shading flat;
xtb1=zeros(dimx,dimy);
xtb1((ahat)>qtb1&(ahat)<qtb2)=1;
figure;surf(xtb1);shading flat;

figure;surf(yhat-yhats(:,:,10));shading flat;
figure;surf(yhats(:,:,1)-yhats(:,:,2));shading flat;