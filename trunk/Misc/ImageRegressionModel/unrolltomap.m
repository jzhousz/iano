function A = unrolltomap(genenumber, stagenumber)
%genenumber = 51;   
%51: ftz %58:ken;  48: eve hb: 55;   gt: 53   %bcd: 
%HBp: 87

load virtualembryo;

switch nargin
    case 1
        st = 6; %take the last stage
    case 2
        st = stagenumber;
end

% May 9, 2012, add all stages.
switch st
    case 1
        co = data(:,2:4);
        expression = data(:,8:39);
    case 2
        co =  data(:,40:42);  %AN, AO, AP
        expression = data(:,43:79);  %AQ to CA
    case 3
        co =   data(:,80:82);  % CB, CC, CD
        expression = data(:,83:131);  % CE, to EA
    case 4
        co =     data(:,132:134);       % EB, EC, ED
        expression =    data(:,135:224);  % EE to HP
    case 5                 
        co =    data(:, 225:227); % HQ HR HS
        expression =  data(:, 228:318);  % HT to LF
    case 6
        co = data(:,319:321);   %LG LH LI
        expression = data(:,325:411);   %LJ -OU
    %what about those mRNA expression?    
end
%s1_co =data(:,2:4);
%stage1 = data(:,8:39);
%s6_co = data(:,319:321);
%stage6 = data(:,325:411);
%%%%%%%%%%%%%%%%%%%%%%%%
%co = s6_co;
%expression = stage6;
%%%%%%%%%%%%%%%%%%%%%%
x = co(:,1);
y = co(:,2);
z = co(:,3);
% long axis is the Z (AP axis)
temp = z;
z = x;
x = temp;
%plot3(x, y, z);

%projection
%http://mathworld.wolfram.com/CylindricalProjection.html
%conver to plonar coordinates for a sphere to get latitude and longitude

%returns latitude degree(heading angle around y-axis) -180 to +180
lambda = sign(y).*acosd(x./sqrt(x.*x+y.*y));

% returns longitude (azimuth or pitch angle) -90 to +90
phi = atand(z./sqrt(x.*x+y.*y));

% size of the image
width = 360;
height = 180;

% unroll to a map
%http://processing.org/discourse/yabb2/YaBB.pl?board=Programs;action=display;num=1218822969
% return new Point(((180+lambda)/360)*width, height - ((90+phi)/180)*height);
unrolledx = ((180+lambda)/360)*width; 
for i=1:size(unrolledx,1)
    if unrolledx(i) > width/2
        unrolledx(i) = unrolledx(i)-width/2;
    else
        unrolledx(i) = unrolledx(i)+width/2;
    end
end
unrolledy = (90+phi)/180*height;


%put in a image
% set some volume around it to fill the background of cell.
r = 1;
A = zeros(height, width, 3);
for i = 1: size(expression,1)
    coordx = uint16(unrolledx(i))+1;
    coordy = uint16(unrolledy(i))+1;
    for m=coordy-r:coordy+r
        for n=coordx-r:coordx+r
    %A(coordy, coordx) = stage1(i,9)*255;
          if (m >0 && m <= height && n>0 && n <= width)
            A(m, n, 1) = expression(i,genenumber)/max(expression(:,genenumber));  %scale to 0-1
            % brighter unless background. Threshold: 10%.
            if (A(m,n,1) > 0.1)
              A(m, n, 1) = min(A(m,n,1)*1.3, 1); 
            end
         end
        end
    end
end


%interploation to avoid zeros
r = 2; % 2r+1 is the size of square kernel for convoluation
for i = 1:height
    for j=1:width
        if (A(i,j,1) == 0)
            sum= 0; total = 0;
            for m=i-r:i+r
                for n=j-r:j+r
                  if (m >0 && m <= height && n>0 && n <= width)
                        sum =sum + A(m, n, 1);
                        total = total +1;
                  end  
                end
         end
           %set the averaged value
            A(i,j,1) =sum/total;
        end
    end
end    

Q = A;

%do another round of smoothing
r = 2; % 2r+1 is the size of square kernel for convoluation
for i = 1:height
    for j=1:width
        %if (A(i,j,1) == 0)
            sum= 0; total = 0;
            for m=i-r:i+r
                for n=j-r:j+r
                  if (m >0 && m <= height && n>0 && n <= width)
                        sum =sum + Q(m, n, 1);
                        total = total +1;
                  end  
                end
          %  end
            %set the averaged value
            A(i,j,1) =sum/total;
        end
    end
end    


image(A);