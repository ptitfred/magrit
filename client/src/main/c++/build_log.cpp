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
/////////////////////////////////////////////////////////////////////////
// MAGRIT 
#include "build_log.hpp"
/////////////////////////////////////////////////////////////////////////
// STD 
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
magrit::log::log ( generic_command* previous_subcommand )
  : generic_command ( previous_subcommand ),
    _log_options ("Log options"),
    _positional_parameters_desc
    ("Positional options (can be added to the end of argument list without the dashed string)")
{
  _log_options.add_options()
    ( "watch,w","activates the automatic refresh" );

  get_options().add ( _log_options );

  _positional_parameters.add("git-args", -1);

  _positional_parameters_desc.add_options()
    ("git-args", boost::program_options::value<std::vector<std::string>>(),
     "[<since>..<until>] git revisions");

  get_options().add ( _positional_parameters_desc );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::log::get_name() const
{
  return "log"; 
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::log::get_description() const
{
  return "<description to be written>";
}

/////////////////////////////////////////////////////////////////////////
const boost::program_options::positional_options_description&
magrit::log::get_positional_options () const
{
  return _positional_parameters;
}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::process_parsed_options
(
  const std::vector<std::string>& arguments,
  const boost::program_options::variables_map& vm,
  bool allow_zero_arguments
)
const
{
  generic_command::process_parsed_options ( arguments, vm, true );

  std::vector< std::string > git_args;

  if ( vm.count ( "watch" ) )
  {
    clear_screen();
  }

  if ( vm.count ( "git-args" ) )
  {
    git_args = vm["git-args"].as< std::vector< std::string > >();
  }

  std::string cmd = "magrit status " + get_repo_name() + " -";
 
  std::vector< std::string > revisions = get_git_commits ( git_args );

  std::for_each 
  (
    revisions.begin(), revisions.end(),
    [](const std::string& rev)
    {
      std::cout << rev << std::endl;
    }
  );

  // TODO: pipe get_git_commits to ssh command input
  //       using boost::process (pipeline_entry and
  //       launch_pipeline).
  /*
  int ssh_handle = send_ssh_command_background ( cmd );

  std::for_each 
  (
    revisions.begin(), revisions.end(),
    [](const std::string& rev)
    {
      rev >> std::cin;
    }
  );

  wait_children ( ssh_handle );*/
}
