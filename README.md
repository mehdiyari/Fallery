# Fallery [![](https://jitpack.io/v/mehdiyari/Fallery.svg)](https://jitpack.io/#mehdiyari/Fallery)

Fallery is A fully customizable media picker for Android apps with many features.

# Demo

![](assets/demo.jpg)

# Key Features

1. Select media(photo, video) from android media store with
   custom [Offline Gallery](http://mehdiyari.ir/2020/08/14/create-a-custom-offline-online-gallery-with-fallery/)
   or
   custom [Online Gallery](http://mehdiyari.ir/2020/08/14/create-a-custom-offline-online-gallery-with-fallery/)
2. Compatible with Android API Level +14
3. Content Observer(When a new media file is added to the device, fallery is informed and shows new
   media in the UI)
4. Filter media based on types(Photos, videos, both)
5. Modern user interface with the capability to add new themes and languages
6. Select media with a text caption
7. Taking photos from the camera with intent
8. Show bucket list in two different UI(Grid and linear), which user can switch between them in
   runtime
9. Support max selectable media
10. Support screen orientation
11. Support custom edits text layout for the text caption(for emoji compatibility, etc.)
12. Support turning on or off media counts in the fallery toolbar
13. Support vertical and horizontal scrolling in showing media view-pager
14. Support custom view-pager transformer in media view-pager
15. Support custom action for video toggle in the video preview screen.
16. Support requests external storage permissions for all Android versions automatically.
17. Support changing the row count of images based on user zoom-in or zoom-out.
18. Support starting Fallery from composable functions.

## Usage

### Gradle

Step 1. Add this in your project root `build.gradle` at the end of repositories:

```gradle
allprojects {
  repositories {
   maven { url 'https://jitpack.io' }
  }
}
```
Step 2. Add the dependency
```gradle
dependencies {
  implementation 'com.github.mehdiyari:Fallery:{latest_version}'
}
```
### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<dependency>
    <groupId>com.github.mehdiyari</groupId>
    <artifactId>Fallery</artifactId>
    <version>{latest_version}</version>
</dependency>
```
### Simple Using
Kotlin
```Kotlin
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val falleryOptions = FalleryBuilder()
            .setImageLoader(YourImageLoader())
            .setMediaObserverEnabled(true)
            .build()

        falleryButton.setOnClickListener {
            startFalleryWithOptions(requestCode = 1, falleryOptions = falleryOptions)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            handleResultWithCaption(
                result = data?.getFalleryResultMediasFromIntent(),
                caption = data?.getFalleryCaptionFromIntent()
            )
        }
    }

    private fun handleResultWithCaption(result: Array<String>?, caption: String?) {
        TODO("handle result and caption")
    }
}
```

Java
```Java
public class MainActivity extends AppCompatActivity {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FalleryOptions falleryOptions = new FalleryBuilder()
                .setImageLoader(YourImageLoader())
                .build();

        this.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fallery.startFalleryFromActivityWithOptions(
                        MainActivity.this, 1, falleryOptions
                );
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String[] result = Fallery.getResultMediasFromIntent(data);
            String caption = Fallery.getCaptionFromIntent(data);
            handleResultWithCaption(result, caption);
        }
    }

    private void handleResultWithCaption(String[] result, String caption) {
        // todo: handle result and caption
    }
}
```

For more details about using Fallery, please check the example module
or [Fallery Blog Post](https://mehdiyari.medium.com/fallery-a-fully-customizable-media-picker-for-android-f391d24ba791).

## Contribute

I would appreciate your contributions to PRs, wiki, issues, etc.

## License

    Copyright 2023 Mehdi Yari

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.