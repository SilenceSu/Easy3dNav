package com.github.silencesu.Easy3dNav;

import java.io.IOException;

/**
 * 兼容老版本
 * @author SilenceSu
 * @Email Silence.Sx@Gmail.com
 * Created by Silence on 2019/12/25.
 */
@Deprecated
public class RecastNav extends Easy3dNav {
    public RecastNav(String filePath) throws IOException {
        super(filePath);
    }
}
