## Fallery version 1.0.2

1. Update library versions
2. Fix issues of preview fragment after device rotate

## Fallery version 1.0.1

1. CleanUp codes
2. Update dependencies
3. Fix some bugs
4. Add a new option to fallery for changing bucket content span-count based on user touch(zoom in, zoom out)
5. Update README.MD

## Fallery version 1.0.0 

#### Compatible with android +10
    1. Package visibility in Android 11 
    2. Storage updates in Android 11 

#### Fix Bugs
     1.  Finish fallery activity if CoreComponent is null(if permissions changed from settings or any case that fallery started without official API) 
     2.  Fix issue of showing error layout when views is on loading state 
     3.  Fix issue of creating two instance of FalleryActivityComponent 
     4.  Fix issue of media store observable 
     5.  Fix bug of resetting viewpager adapter position after onStop 
     7.  Fix animation of sending media footer 
     8.  Fix memory leak of gridLayoutManagers in BucketContentFragment & BucketListFragment 
     9.  Fix lint errors and warnings 
     10. Fix memory leak of bottomNavigationDrawerFragment in MainActivity 
     11. Fix failed tests in BucketListViewModelTest.kt 
     12. Remove unused classes 
    
#### Update third-party libraries
    1. Update gradle to 6.8.3 
    2. Update kotlin version to 1.4.32 
    3. Update android gradle plugin to 4.1.3  
    4. Update fallery example app dependencies to latest versions 
    5. Run kotlin migration to 1.4.32 
    6. Update fallery libraries version 
    7. Update compile and target sdk to 30 
    8. Update kotlinx-coroutines-test dependency to 1.4.1 
    

## Fallery version 0.9.2

Code cleanup and optimization

## Fallery version 0.9.1

Fix issue When the screen rotate. The current photo is not displayed <br>
Fix the issue of showing recyclerViewItemMode menuItem <br>

## Fallery version 0.9.0

First Release