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
#include "utils.hpp"
#include "wait.hpp"
/////////////////////////////////////////////////////////////////////////
// STD 
#include <iomanip>
#include <time.h>
/////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////
void clear_screen_linux ()
{
  std::cout << "\x1b[0;0H\x1b[2J";
}

/////////////////////////////////////////////////////////////////////////
void move_up_linux ( size_t num )
{
  std::cout << "\x1b[" << num << "A";
}

/////////////////////////////////////////////////////////////////////////
void print_date ()
{
  time_t epoch_seconds = time ( NULL );

	std::cout << "Last update: "
            << asctime ( localtime ( &epoch_seconds ) ) ; 
}

/////////////////////////////////////////////////////////////////////////
magrit::log::log ( generic_command* previous_subcommand )
  : generic_command ( previous_subcommand, true ),
    _log_options ("Log options")
{
  _log_options.add_options()
    ( "watch,w","activates the automatic refresh" );

  get_options().add ( _log_options );
}

/////////////////////////////////////////////////////////////////////////
const char*
magrit::log::get_name() const
{
  if ( _previous_subcommand == nullptr )
  {
    return "magrit-build-log";
  }
  else
  {
    return "log"; 
  }
} 

/////////////////////////////////////////////////////////////////////////
const char* magrit::log::get_description() const
{
  return "Shows the status of the commits sent to the server";
}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::process_parsed_options
(
  const std::vector<std::string>& arguments,
  const boost::program_options::variables_map& vm,
  const std::vector<std::string>& unrecognized_arguments,
  bool allow_zero_arguments
)
const
{
  generic_command::process_parsed_options
    ( arguments, vm, unrecognized_arguments, true );

  if ( vm.count ( "watch" ) )
  {
    watch_status ( unrecognized_arguments );
  }
  else
  {
    print_status ( unrecognized_arguments );
  }
}

/////////////////////////////////////////////////////////////////////////
std::string
colorize_linux ( const std::string& status, bool color )
{
  std::stringstream output;

  for ( size_t i = 0 ; i < status.size() ; ++i )
  {
    switch ( status[i] )
    {
      case 'O':
        output << cool ( status[i], color );
        break;
      case 'E':
        output << error ( status[i], color );
        break;
      case 'R':
        output << running ( status[i], color );
        break;
      case 'P':
        output << pending ( status[i], color );
        break;
      case '?':
        output << warning ( status[i], color );
        break;
      default:
        output << status[i];
        break;
    }
  }

  return output.str();
}

/////////////////////////////////////////////////////////////////////////
void
print_status_line ( const std::string& desc, const std::string& status, bool color )
{
  auto msg_width
    = color? magrit::get_message_max_width () + 8
           : magrit::get_message_max_width();

  std::cout 
    << std::left << std::setw ( msg_width )
    << magrit::cut_message ( desc, msg_width ) << " | "
    << colorize_linux ( status , color )
    << std::endl;

}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::watch_status ( const std::vector < std::string >& git_args )
const
{
  clear_screen_linux();

  while ( true )
  {
    auto sha1s = get_commits ( git_args );

    get_status
    (
      git_args,
      [&] ( const std::string& commit_desc, const std::string& status )
      {
        print_status_line ( commit_desc, status, color ); 
      }
    );

    print_date();

    wait::wait_for ( "SEP", 0, sha1s, true );
    
    move_up_linux ( sha1s.size() + 1 );
  }
}

/** Used only by get_status */
template < class Stream >
void
print_status_lines 
(
  const std::vector < std::string >& git_args,
  Stream& status_output,
  std::function
    <void (const std::string& commit_desc,const std::string& status)> func,
  bool color
)
{
  // We issue again a git log with color and the commit message.
  // For every line, we print the status previously fetched from
  // server. Note: it's theoretically possible that the previous
  // git log had less lines than the following one if a commit 
  // was pushed in between, but in practice the odds are very low
  // and the impact is very small.
  magrit::start_git_process
  (
    std::vector < std::string >
    {
      "log", color?"--color=always":"--color=never", "--oneline", "-z",
      join ( " ", git_args.begin(), git_args.end() )
    },
    bp_inherit(), bp_capture(), bp_inherit(),
    [&]( const std::string& line )
    { 
      std::string status;
      std::getline( status_output, status );
      func ( line, status );
    },
    true
  );
}
/////////////////////////////////////////////////////////////////////////
void
magrit::log::get_status
( 
  const std::vector < std::string >& git_args,
  std::function
    <void (const std::string& commit_desc,const std::string& status)> func 
) const
{
  std::vector < boost::process::pipeline_entry > pipeline;

  pipeline.push_back ( get_commits_pipeline ( git_args ) ); 

  pipeline.push_back
  ( 
    create_pipeline_member
    (
      "ssh",
      std::vector < std::string >
      {
        "-x", "-p",
        boost::lexical_cast<std::string> ( get_magrit_port() ),
        get_magrit_connection_info(),
        "magrit", "status", get_repo_name(), "-"
      },
      bp_close(), bp_capture(), bp_inherit()
    )
  );

  boost::process::children statuses = start_pipeline ( pipeline );

  print_status_lines ( git_args, statuses.back().get_stdout(), func, color );
}

/////////////////////////////////////////////////////////////////////////
void
magrit::log::print_status ( const std::vector < std::string >& git_args )
const
{
  get_status
  (
    git_args, 
    [&] ( const std::string& commit_desc, const std::string& status )
    {
      print_status_line ( commit_desc, status, color );
    }
  );
}


