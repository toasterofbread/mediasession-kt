#include <wchar.h>

int init();

void setOnPlay(void (*func)(void*));
void setOnPause(void (*func)(void*));
void setOnStop(void (*func)(void*));
void setOnNext(void (*func)(void*));
void setOnPrevious(void (*func)(void*));

void setOnRateChanged(void (*func)(double, void*));
void setOnShuffleChanged(void (*func)(int, void*));
void setOnLoopChanged(void (*func)(unsigned int, void*));

void setCallbackData(void*);
void revokeCallbacks();

void setOnSeek(void (*func)(long long, void*));

int getEnabled();
void setEnabled(int);

int getNextEnabled();
void setNextEnabled(int);

int getPreviousEnabled();
void setPreviousEnabled(int);

int getPlayEnabled();
void setPlayEnabled(int);

int getPauseEnabled();
void setPauseEnabled(int);

int getStopEnabled();
void setStopEnabled(int);

double getRate();
void setRate(double);

int getShuffle();
void setShuffle(int);

unsigned int getLoop();
void setLoop(unsigned int);

unsigned int getPlaybackState();
void setPlaybackState(unsigned int);

void setTimelineProperties(long long, long long, long long, long long);
void setPosition(long long);

void update();
void reset();

int getMediaType();
void setMediaType(int);

int thumbnailLoaded();
void setThumbnail(const wchar_t*, int);

const wchar_t* getMusicTitle();
void setMusicTitle(const wchar_t*);

const wchar_t* getMusicArtist();
void setMusicArtist(const wchar_t*);

const wchar_t* getMusicAlbumTitle();
void setMusicAlbumTitle(const wchar_t*);

const wchar_t* getMusicAlbumArtist();
void setMusicAlbumArtist(const wchar_t*);

unsigned int getMusicGenresSize();
const wchar_t* getMusicGenreAt(unsigned int i);
void addMusicGenre(const wchar_t*);
void clearMusicGenres();

unsigned int getMusicAlbumTrackCount();
void setMusicAlbumTrackCount(unsigned int);

unsigned int getMusicTrack();
void setMusicTrack(unsigned int);
