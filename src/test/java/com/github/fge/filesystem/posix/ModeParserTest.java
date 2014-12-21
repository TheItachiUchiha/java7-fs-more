package com.github.fge.filesystem.posix;


import com.github.fge.filesystem.exceptions.InvalidModeInstructionException;
import com.github.fge.filesystem.helpers.CustomAssertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

import static com.github.fge.filesystem.helpers.CustomAssertions.shouldHaveThrown;
import static org.assertj.core.api.Assertions.assertThat;

public final class ModeParserTest
{
    private static final Set<PosixFilePermission> NO_PERMISSIONS
            = EnumSet.noneOf(PosixFilePermission.class);
            
    @DataProvider
    public Iterator<Object[]> invalidModeInstructions()
    {
        final List<Object[]> list = new ArrayList<>();

        list.add(new Object[]{ "" });
        list.add(new Object[]{ "ur" });
        list.add(new Object[]{ "u+" });
        list.add(new Object[]{ "t+r" });
        list.add(new Object[]{ "uw-r" });
        list.add(new Object[]{ "u-y" });

        return list.iterator();
    }

    @Test(dataProvider = "invalidModeInstructions")
    public void invalidModeInstructionThrowsAppropriateException(
        final String instruction)
    {
        final Set<PosixFilePermission> toAdd
            = EnumSet.noneOf(PosixFilePermission.class);
        final Set<PosixFilePermission> toRemove
            = EnumSet.noneOf(PosixFilePermission.class);

        try {
            ModeParser.parseOne(instruction, toAdd, toRemove);
            shouldHaveThrown(InvalidModeInstructionException.class);
            shouldHaveThrown(UnsupportedOperationException.class);
        } catch (InvalidModeInstructionException e) {
            assertThat(e).isExactlyInstanceOf(
                InvalidModeInstructionException.class).hasMessage(instruction);
        }catch (UnsupportedOperationException e) {
            assertThat(e).isExactlyInstanceOf(
                    UnsupportedOperationException.class).hasMessage(instruction);
        }
    }

    @Test
    public void unsupportedModeInstructionThrowsAppropriateException()
    {
        String instruction = "u-X";
        
        final Set<PosixFilePermission> toAdd
                = EnumSet.noneOf(PosixFilePermission.class);
        final Set<PosixFilePermission> toRemove
                = EnumSet.noneOf(PosixFilePermission.class);

        try {
            ModeParser.parseOne(instruction, toAdd, toRemove);
            shouldHaveThrown(UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e) {
            assertThat(e).isExactlyInstanceOf(
                    UnsupportedOperationException.class).hasMessage(instruction);
        }
    }

    @DataProvider
    public Iterator<Object[]> validModeInstructions()
    {
        final List<Object[]> list = new ArrayList<>();
        
        list.add(new Object[]{"ug+r", EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ), NO_PERMISSIONS});
        list.add(new Object[]{"ug-r", NO_PERMISSIONS, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ)});
        list.add(new Object[]{"o-x", NO_PERMISSIONS, EnumSet.of(PosixFilePermission.OTHERS_EXECUTE)});
        list.add(new Object[]{"uog-rxw", NO_PERMISSIONS, EnumSet.of(
                PosixFilePermission.OWNER_READ,PosixFilePermission.OWNER_EXECUTE,PosixFilePermission.OWNER_WRITE
                ,PosixFilePermission.OTHERS_READ,PosixFilePermission.OTHERS_EXECUTE,PosixFilePermission.OTHERS_WRITE
                ,PosixFilePermission.GROUP_READ,PosixFilePermission.GROUP_EXECUTE,PosixFilePermission.GROUP_WRITE)});

        return list.iterator();
    }

    @Test(dataProvider = "validModeInstructions")
    public void validModeInstructionAddsInstructionsInAppropriateSets(
            final String instruction, Set<PosixFilePermission> add,
            Set<PosixFilePermission> remove)
    {

        final Set<PosixFilePermission> toAdd
                = EnumSet.noneOf(PosixFilePermission.class);
        final Set<PosixFilePermission> toRemove
                = EnumSet.noneOf(PosixFilePermission.class);

        ModeParser.parseOne(instruction, toAdd, toRemove);

        assertThat(toAdd).isEqualTo(add);
        assertThat(toRemove).isEqualTo(remove);

    }
}
