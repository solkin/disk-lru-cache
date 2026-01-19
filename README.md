# Disk LRU Cache [![](https://jitpack.io/v/solkin/disk-lru-cache.svg)](https://jitpack.io/#solkin/disk-lru-cache) [![Android Arsenal]( https://img.shields.io/badge/Android%20Arsenal-Disk%20LRU%20Cache-green.svg?style=flat )]( https://android-arsenal.com/details/1/7454 )

Disk LRU (least recently used) cache with persisted journal. 
This cache has specific capacity and location.
Rarely requested files are evicted by actively used.

Lightweight and extremely easy to use.

![Cache icon](/cache_icon.png)

### Add dependency
**Step 1.** Add the JitPack repository to your build file
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
**Step 2.** Add the dependency
```groovy
implementation 'com.github.solkin:disk-lru-cache:<version>'
```
Replace `<version>` with the latest version from the JitPack badge above.

### Create DiskLruCache
```java
long CACHE_SIZE = 500 * 1024; // Size in bytes
DiskLruCache cache = DiskLruCache.create(getCacheDir(), CACHE_SIZE);
```

### Add file into cache
To manage some files by cache you just need to invoke `put` method like any `Map`.

Key - any string to request this file from cache.

File - file, that will be moved into cache.

```java
String key = "some-key";
File file = File.createTempFile("random", ".dat");
cache.put(key, file);
```

### Getting file from cache
To get file from cache, just invoke `get` method. Yes, also like any `Map`.

Key is the same you put this file into cache

This method will return `File` you put into cache or `null`, if file was evicted from cache.

```java
String key = "some-key";
File file = cache.get(key);
```

### Delete file from cache
To delete file from cache, just invoke `delete` method.

Key is the same you put this file into cache.

File will be deleted from cache and from journal.

Throws `RecordNotFoundException` if the key is not found in cache.

```java
String key = "some-key";
try {
    cache.delete(key);
} catch (RecordNotFoundException e) {
    // Key not found in cache
}
```

### Clear cache
Sometime you may need to clear whole cache and drop all stored files.

```java
cache.clearCache();
```

### List keys in cache
To get all keys, managed by cache, invoke `keySet()` method.

This will return `Set<String>`.

List all keys in cache may be useful to check all files, stored in cache. 

```java
Set<String> keys = cache.keySet();
```


### Get cache status information
There are some useful cache status information, that you can request.

```java
cache.getCacheSize(); // Cache size in bytes, that you set up on cache creation.
cache.getUsedSpace(); // Size of all files, stored in cache.
cache.getFreeSpace(); // Free size in cache.
cache.getJournalSize(); // Internal cache journal size in bytes.
```

### Thread safety
DiskLruCache is thread-safe. All public methods are synchronized and can be safely called from multiple threads.

### Limitations
- File size cannot exceed cache size. Attempting to put a larger file will throw `IOException`.
- Key cannot be `null` or empty. Invalid keys will throw `IllegalArgumentException`.

### Requirements
- Min SDK: 16 (Android 4.1)
- Target SDK: 34

### Custom FileManager and Logger
You can provide custom implementations of `FileManager` and `Logger`:

```java
FileManager fileManager = new MyCustomFileManager();
Logger logger = new MyCustomLogger();
DiskLruCache cache = DiskLruCache.create(fileManager, logger, CACHE_SIZE);
```

### License
    MIT License
    
    Copyright (c) 2022-2026 Igor Solkin
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
