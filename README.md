# Improving Exposure Fusion Algorithm for High Dynamic Range (HDR) Imaging 

[paper](https://sally20921.github.io/doc/HDR/HDR_paper.pdf)

[poster](https://sally20921.github.io/doc/HDR/presentation2.jpeg)

![image](https://user-images.githubusercontent.com/38284936/128335122-81978822-ca51-4daf-89c3-75650e5854cf.png)

## Introduction
- digital cameras have a limited dynamic range, which is lower than one encounters in the world
- in high dynamic range scenes, a picture will often turn out to be under- or overexposed
- Exposure Fusion has been proposed to skip the steps of tone mapping by computing a perceptual quality measure for each pixel which encodes desirable qualities

## Exposure Fusion
- assume images are perfectly aligned, possibly using registration algorithm
- for each pixel, combine the information from different measures (contrast, saturation, well-exposedness) into a scalar weight map using multiplication. 
- weighting exponents being ω, i,j,k refers to pixel (i,j) in the k-th image

![image](https://user-images.githubusercontent.com/38284936/128335245-d0cb1af8-0c1b-4f48-bafe-1f7d8b0b0f87.png)

![image](https://user-images.githubusercontent.com/38284936/128335264-34c4e217-e007-44d2-98fe-2fec1e358bce.png)

![image](https://user-images.githubusercontent.com/38284936/128335284-9b57fa04-5b6d-4116-988c-7cd984ef674f.png)

![image](https://user-images.githubusercontent.com/38284936/128335319-5383bf21-67c4-45a3-91bc-aa22232b3471.png)

- normalize the values of the N weight maps such that they sum to one at each pixel (i,j)
- resulting image R can be obtained by a weighted blending of the input images I

```
# pseudo-code of Merten's Exposure Fusion Algorithm
C = scale(contrast(images))
S = scale(saturation(images))
W = scale(well-exposedness(images))

Quality_measure = computeWeightMap(C,S,W)

for each image I in the images do 
  pyr = Laplacian_pyr(I)
  pyrG = Gaussian_pyr(Quality_measure)
  for level L in the number of levels do 
    R[L] = pyr[L] * pyrG[L]
  end for
end for
```


