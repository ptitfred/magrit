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

struct cat_build
{
  static void run ( const char* rev = NULL )
  {
    check_ssh ();

    repo=${_target[_REPO]}

    std::string sha1 = git_get_rev_hash ( rev );

    send_ssh_command ( join(" ","magrit cat-build",repo,sha1) );
  }

  static void git_get_rev_hashes ( const char* rev )
  {
    log_error << "TODO" << std::endl;
    /*
    if ( rev == NULL )
    {
      rev_str = "HEAD";
    }
    else
    {
      rev_str = rev;
    }

    sha1=$(git rev-parse --verify $revstr)
    */
};

