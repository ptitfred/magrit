/**
 * Copyright 2011 Frederic Menou
 *
 * This file is part of Magrit.
 *
 * Magrit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Magrit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Magrit.
 * If not, see <http://www.gnu.org/licenses/>.
 */
///////////////////////////////////////////////////////////////////////////
// MAGRIT 
#include "magrit-send-build.hpp"
#include "magrit-cat-build.hpp"
///////////////////////////////////////////////////////////////////////////

struct build_log
{
  static void run ()
  {

      if [ "$1" = "--help" -o "$1" = "-h" ]
      then
        echo "usage: magrit build-log [--watch] [<git-log options>] [<since>..<until>]"
        cat <<_HELP_
        --watch		to activate automatic refresh
      _HELP_
        exit 0
      fi

      _checkSsh

      function eraseScreen {
        printf "\x1b[0;0H\x1b[2J"
      }

      watch=0
      if [ "$1" = "--watch" ]
      then
        watch=1
        shift 1
        eraseScreen
      fi

      _colorize "## log" 37

      repo=${_target[$_REPO]}

      in="/tmp/magrit-${BASHPID}-in"
      out="/tmp/magrit-${BASHPID}-out"

      mkfifo $in $out

      position=-1
      goon=1
      working=1

      function openStream {
        exec 3<$out
      }

      function closeStream {
        echo "--" >$in
        exec 3<&- 2>/dev/null

        for pid in $(jobs -rp); do
          kill -s TERM $pid
        done
      }

      function clean {
        [ $working -eq 1 ] && closeStream

        rm -f $in $out
      }

      function ctrlC {
        if [ $position -ge 0 ]
        then
          _moveUp 1
          _moveDown $position
        fi
        _moveLeft 2
        printf $eraseEOL

        if [ $position -ge 0 ]; then
          echo "Bye."
        else
          echo "Interrupted."
        fi
        clean
        _moveDown $commitsCount
      }

      trap ctrlC SIGINT

      function printDate {
        echo "Last update: $(date +%H:%M:%S)"
      }

      _sendSshCommandBg $in $out magrit status $repo -

      openStream
      commits=$(git log --format=%H $1)
      commitsCount=0
      for sha1 in $commits
      do
        if [ $goon -eq 1 ]
        then
          log=$(git log --color=$_colorAction -1 --oneline $sha1 -z)
        
          echo $sha1 >$in
          read -u 3 buildStatus 2>/dev/null
          if [ $? -gt 0 ]; then
            #echo "Interrupted"
            goon=0
          else
            echo -e "$(_ellipsis "$log") | $(_colorizeStatus $buildStatus)"
            let "commitsCount += 1"
          fi
        fi
      done

      closeStream

      if [ $watch -eq 1 ]
      then
        # Ctrl-C to exit this loop
        while [ $goon -eq 1 ]
        do
          printDate
          # this counter is here to make interruption handling cleaner
          # the "Bye." message can be place after the build log even if an update of the log is in progress
          working=0
          _sendSshCommand --raw magrit wait-for --event-mask=SEP --timeout=30000 $repo $commits >/dev/null
          working=1
          position=0
          _moveUp 1
          _moveUp $commitsCount

          set -x
          _sendSshCommandBg $in $out magrit status $repo -
          set +x
          openStream
          for sha1 in $commits
          do
            # read status for a commit
            echo $sha1 >$in
            read -u 3 buildStatus 2>/dev/null
            if [ $? -gt 0 ]; then
              goon=0
            else
              # move the cursor after the pipe sign
              _moveRight ${_logWidth}
              if [ ${_color} -eq 1 ]
              then
                _moveLeft 4
              else
                _moveRight 4
              fi

              # clean end of line from this point
              printf $eraseEOL

              echo -e $(_colorizeStatus $buildStatus)

              let "position += 1"
            fi
          done
          closeStream
          [ $goon -eq 1 ] && printf $eraseEOL
        done
      else
        clean
      fi
  }
};

