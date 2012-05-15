twodeve = eve(:,:,1);
blk = find(twodeve <= 0.25)
white = find(twodeve > 0.25)
test = twodeve;
test(blk) = 0;
test(white) = 255;
imshow(test);
BWoutline = bwperim(test);
Segout = eve(:,:,1);
Segout(BWoutline) = 255;
figure, imshow(Segout), title('outlined original image');