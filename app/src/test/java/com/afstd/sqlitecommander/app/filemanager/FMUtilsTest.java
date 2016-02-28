package com.afstd.sqlitecommander.app.filemanager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by pedja on 23.1.16..
 */
@RunWith(MockitoJUnitRunner.class)
public class FMUtilsTest
{
    @Test
    public void filetype_isCorrect()
    {
        assertThat(FMUtils.getFileType("/data/app/com.tehnicom/base.apk"), is(FMUtils.FileType.application));
        assertThat(FMUtils.getFileType("/data/app/com.tehnicom/base"), is(FMUtils.FileType.unknown));
        assertThat(FMUtils.getFileType("base.apk"), is(FMUtils.FileType.application));
        assertThat(FMUtils.getFileType("base/"), is(FMUtils.FileType.unknown));
        assertThat(FMUtils.getFileType("base/apk"), is(FMUtils.FileType.unknown));
        assertThat(FMUtils.getFileType("base/base.apk"), is(FMUtils.FileType.application));
        assertThat(FMUtils.getFileType("base /base.apk"), is(FMUtils.FileType.application));
        assertThat(FMUtils.getFileType("base //base.apk"), is(FMUtils.FileType.application));
        assertThat(FMUtils.getFileType("base //base"), is(FMUtils.FileType.unknown));
    }
}
