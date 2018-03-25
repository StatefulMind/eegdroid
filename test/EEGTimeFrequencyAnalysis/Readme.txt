The main functionality of this App is generation of a time-frequency-map for eeg data.

Bitmap getSpectrogramBitmap(double []eegData,
                            int fs,
                            int width,
                            int overlap,
                            String window,
                            boolean mean,
                            double maxHertz,
                            boolean fftw)

eegData : 1 dimensional real valued EEG input data
fs      : EEG sampling rate
width   : window width for a single fft in the convolution
overlap : the overlap of consecutive windows
window  : the window smoothing filter (hanning, hamming or none)
mean    : indicates whether the mean should be subtracted after the filtering
maxHertz: the maximum frequency value that is supposed to be displayed (in Hz)
fftw    : indicates whether to use the wrapper for the high performance fft library fftw *

* There are tow fft versions available. A custom version written by myself (reference:
http://jakevdp.github.io/blog/2013/08/28/understanding-the-fft/), which is actually only a fast-ft
for input sizes of 2^x and uses a normal dft otherwise. And I found a java wrapper for the high
performance fftw library which is written in C (reference:
https://github.com/bytedeco/javacpp-presets/tree/master/fftw).

The view in android contains right now 3 dropdowns, one for the EEG channel, the width and the
overlap (width can be chosen in seconds and overlap is proportional to the width). Dropdowns for the
other options could be added easily. The data that is visualized was collected from Johannes and
Olivera and is read from an csv file. I cut off the lowest three frequencies which is also visible in
the heatmap (can easily be undone). The values for those frequencies are so high that differences
between the other values would become invisible (log scaling did not help).

(1) There are basically 3 steps involved to generate the chart. The getSpectrogramBitmap() generates
a time-frequency matrix, which is converted into a Bitmap. The magnitudes are min-max scaled and
transferred into the HSV colorspace creating a heatmap (the he is just proportional to the
magnitude). (2) The Bitmap is then resized to fit the size of the parent in the android view
(respects the space dor the axis descriptions) (3) The axis descriptions is then drawn from scratch
onto the Bitmap (I could not find any free heatmap tool for android that is able to manage large
matrices sensibly).

The code consists of a utilities package which contains all the functions for the required
computations and the single "android activity" (xml setting up the UI) with the class setting it up
and handling the interactions. The package is distribute into three classes. CustomFFT contains my
own fft implementation FFTWWrapper contains the wrapper for the faster-FFT, and Utilities contains
all other functionality mainly the functions for the spectogram and for adding the axis descriptions.