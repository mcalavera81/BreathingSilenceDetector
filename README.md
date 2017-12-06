# Apnoea pauses extractor

## Prerequisites
Docker 17.05 or higher.

## Usage

1. Prepare input files in the same input folder.
    1. Input csv file. File paths inside the csv are relative to the input folder.    
    2. acurable files.    
2. Build container with: `docker build -t acurable .`
3. Execute with: `docker run  -v <input-folder>:/opt/acurable acurable <input>.csv <output>.csv`
    * Input and output csvo files are relative to the input folder.


### Sample execution

#### Sample input csv file:  audiofiles.in.csv

| File Path     | 
| ------------- |
| audio-1.wav   |
| audio-3.wav   | 

 
`docker run -v /opt/acurable:/opt/acurable acurable audiofiles.in.csv pauses.out.csv`



### Sample output csv file: pauses.out.csv

| File Path | Pause #| start [secs]| end [secs]| duration [secs] | type |
| ----------|:------:|:---------:|:---------:|:---------------:|------|
| audio-1.wav| 0 |1.65|2.38|0.74|NORMAL|
| audio-1.wav| 1 |6.04|7.27|1.24|NORMAL|
| audio-1.wav| 2 |10.83|15.81|4.99|**APNOEA**|
| audio-1.wav| 3 |19.12|19.91|0.80|NORMAL|
| audio-3.wav|0 |1.13|2.22|1.09|NORMAL|
| audio-3.wav|1 |5.26|6.51|1.25|NORMAL|
| audio-3.wav|2 |9.54|10.64|1.10|NORMAL|
| audio-3.wav|3 |13.77|14.79|1.02|NORMAL|
| audio-3.wav|4 |17.66|18.45|0.79|NORMAL|
| audio-3.wav|5 |22.10|22.97|0.87|NORMAL|
| audio-3.wav|6 |26.27|27.23|0.96|NORMAL|
| audio-3.wav|7 |30.85|31.65|0.80|NORMAL|
| audio-3.wav|8 |34.87|35.74|0.87|NORMAL|
| audio-3.wav|9 |39.04|39.99|0.95|NORMAL|
| audio-3.wav|10 |43.61|44.72|1.11|NORMAL|
| audio-3.wav|11 |48.37|49.52|1.15|NORMAL|
| audio-3.wav|12 |52.65|57.67|5.02|**APNOEA**|
| audio-3.wav|13 |61.24|62.32|1.08|NORMAL|

### Logs for each file processed
[INFO ] 2017-11-27 14:33:46.978 [main] Main - Start processing...  
\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*  
File: /opt/acurable/audio-1.wav 
Size: 923 Kbytes  
Channels: 1, Frames: 472941 
IO State: READING  
Sample Rate: 22050, Block Align: 2  
Bit Rate: 352 kb/s  
Duration: 00:00:21.448  
Valid Bits: 16, Bytes per sample: 2  
\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*   
[INFO ] 2017-11-27 14:33:47.094 [main] WavFileVolume - Starting processing file: /opt/acurable/audio-1.wav  
[INFO ] 2017-11-27 14:33:47.124 [main] WavFileVolume - Finished processing file: /opt/acurable/audio-1.wav  
[INFO ] 2017-11-27 14:49:41.672 [main] Main - End processing...  

## Report
#### Algorithm complexity
The algorithm complexity is linear O(n) with the file length in terms of time.

Although the frames are read one by one, the file input stream is read in chunks on a 4KB buffer 
for I/O efficiency. In terms of Big O notation is constant O(1).

*To sum up, linear in time, constant in space*.

#### Classification rules
I have implemented a na�ve classification based on a hardcoded length in time to discern the type of breathing pause.

#### Scalable solution
The scalable solution can framed as an __embarrassingly parallel__ workload.

In a kind of map-reduce fashion, we could send all files to a task queue. Have a dispatcher create a worker container for each file and expose only that file
to the container with a volume, also creating a custom input csv file with only one entry (ConfigSets in kubernetes). 
This way each container (Job in kubernetes) would be a one-off task (map phase). When the queue is empty the dispatcher would collect
all outputs and put them all togeether in a single csv document (reduce phase).

#### Limitations
The classification rule is not based on a corpus of audio files.

To have a good characterization of the breathing pauses we would need a training set. Even better
a training set for every person and apply a machine learning algorithm to determine the right model for classification.

#### Future work

Much more testing is needed. Although I managed to create a single end-to-end test.
This testing in turn would force to a refactoring to a more interface/contract oriented programming.

Parameterize some values defined currently in **app-config.properties**:
* sampling: seconds that separate each sample. You may want more precision in the results.
* signalThreshold: RMS value under which a pause is considered valid.
* timeThreshold: breathing pause time over which is considered abnormal, and thus an apnoea.  

Preprocessing audio files: Canceling the background node and filtering out-of-range frequencies.

Perform a frequency analysis to see patterns that could help in the task of pause classification.

Failure handling with bulkheads.

Resume logic after failure detection.# BreathingSilenceDetector
