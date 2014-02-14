/*
 * Copyright 2014. Vladislav Dolgikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elusive_code.newsboy.test;

public class TestHelper {

    public static void progress(float progress) {
        progress(50,progress);
    }

    public static void progress(int width, float progress) {

        System.out.print("\r[");
        int i=0;
        for (;i<=width*progress;i++) {
            System.out.print("=");
        }
        for(;i<width;i++){
            System.out.print(" ");
        }
        System.out.printf("] %3d%%",(int)(progress*100));
    }
}
