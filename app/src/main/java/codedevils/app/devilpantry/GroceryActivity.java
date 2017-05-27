package codedevils.app.devilpantry;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
/**
 * Copyright © 2017 CodeDevils,
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Purpose: The Devil Pantry app will allow a user to keep track of pantry
 * items by scanning barcodes with the device camera; utilize the pantry to
 * allow a user to view recipes using available ingredients; and allow a user
 * to create a shopping list based on all the above.
 *
 * @author CodeDevils team
 * @version Summer 2017
 */
public class GroceryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);
    }
}
