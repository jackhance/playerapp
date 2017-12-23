// IMusicPlayerService.aidl
package com.example.mike.mobileplayer;

// Declare any non-default types here with import statements

interface IMusicPlayerService   {

            
                  /**
                 * 根据位置打开音频文件
                 *
                 * @param position
                 */
                  void openAudio(int position);

                /**
                 * 播放音乐
                 */
                  void start();

                /**
                 * 暂停音乐
                 */
                  void pause();   
            
                //停止
                  void stop();   
            
                //播放上一首
                  void playpre();   
            
                //播放下一首
                  void playnext();   
            
                //得到当前播放进度
                  int getCurrentPosition();   

                //得到总时长
                  int getDuration();

                //得到歌手名
                  String getArtist();  

                //得到歌曲名
                  String getName();  

                //得到音乐路径
                  String getMusicPath();  

                //设置播放模式
                  void setPlayMode(int PlayMode);
                 
                //得到播放模式

                   int getPlayMode();

                  //音频播放的状态判断
                   boolean isPlaying();

                   void seekTo(int position);

                   //得到当前音量
                   int getCurrentVoice();

                   //得到最大音量
                   int getMaxVoice();

}
 
