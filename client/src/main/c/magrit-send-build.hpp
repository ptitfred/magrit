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
// FIFO suppport
#include <sys/types.h>
#include <sys/stat.h>
///////////////////////////////////////////////////////////////////////////

struct send_build
{

  /**
   * Sends a new build. 
   */
  static void run ( const global& global_options, const char* command_line )
  {
    const char* force, command, rev;

    check_ssh();

    log_warning << colorize("## send build",37) << std::endl;

    process_command_line ( command_line, force, command, rev );

    const char* repo = ${_target[$_REPO]}

    make_fifos ( in, out );

    std::stringstream ssh_command;

    ssh_command << join(" ",in,out,"magrit send-build",force,command,repo); 

    send_ssh_command_bg ( ssh_command );

    // TODO:
    //log_git_output << out << std::endl;

    in << git_get_commit_hashes ( rev );

    in << "--";

    // TODO: what's this doing?
    /*exec 3<&- */

    clean_fifos ( in, out );

    clean_jobs ();
  }


  static void process_command_line ()
  {
    log_error << "TODO" << std::endl;
    /*
    while [ "${1:0:2}" = "--" ]; do
      if [ "$1" = "--force" ]; then
        force="$1"
        shift 1
      elif [ "$1" = "--command" ]; then
        command="$1 $2"
        shift 2
      fi
    done

    if [ $# -gt 0 ]; then
      revstr=$1
      count=$(git rev-parse $1 | wc -l)
      if [ $count -eq 1 ]; then
        revstr="-1 $1"
      fi
    else
      revstr="-1 HEAD"
    fi
    */
  }

  static void check_ssh ()
  {
    log_error << "TODO" << std::endl;
  } 

  static void clean_jobs ()
  {
    log_error << "TODO" << std::endl;
    /*for pid in $(jobs -p); do
      kill -s TERM $pid
    done*/
  }

  static void make_fifos ( )
  {
    log_error << "TODO" << std::endl;
    /*const char* in   = "/tmp/magrit-${BASHPID}-in";
    const char* out  = "/tmp/magrit-${BASHPID}-out";*/
    // return a fstream.
  }
  
  static void clean_fifos ( std::fstream& in, std::fstream& out )
  {
    log_error << "TODO" << std::endl;
    /*rm $in $out*/
  }

  static void git_get_commit_hashes ( const char* rev )
  {
    log_error << "TODO" << std::endl;
    /*
    for sha1 in $(git log --format=%H $revstr); do
      echo $sha1 >$in
      read -u 3 status
      
      if [[ "$status" =~ "ssh error: *" ]]; then
        echo $status >&2
      else
        statusText=$(_colorize "Submitted" 92)
        if [ "$status" -eq 0 ]; then
          statusText=$(_colorize "Skipped" 37)
        fi

        log=$(git log --color=$_colorAction -1 --oneline $sha1 -z)
                
        echo -e "$(_ellipsis "$log") | $statusText"
      fi
      
    done
    */
  }
};
