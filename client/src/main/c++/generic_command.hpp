/**
 * Copyright 2012 Frederic Menou
 * Copyright 2012 Daniel Perez
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
// STD
#include <stdexcept>
#include <iostream>
#include <string>
#include <vector>
/////////////////////////////////////////////////////////////////////////
// BOOST
#include <boost/program_options.hpp>
#include <iterator>
/////////////////////////////////////////////////////////////////////////

struct DoNotContinue
{
};

/**
 * Base class for all magrit commands.
 */
struct generic_command
{
  /**
   * Name of the command as it appears in the command line.
   *
   * @return Name of the command.
   */
  virtual const char* get_name() const = 0;

  /**
   * Runs the command. The default behavior is to parse the command line
   * supplied and pass the parsed command line to
   * generic_command::process_parsed_options.
   *
   * @throws boost::program_options::unknown_option if one of the
   *         given command line switches is not allowed.
   */
  virtual void run ( int argc, char** argv ) const
  {
    boost::program_options::variables_map vm;

    boost::program_options::options_description
      parent_options_desc ("");

    parent_options_desc
      .add ( create_options() );

    // Positional commands have to be added to the
    // line parser.
    if ( get_subcommands().size() > 0 )
    {
      boost::program_options::options_description
        positional_options_desc ( "Positional options" );

      positional_options_desc.add_options()
        ("command","positional parameter 0")
        ("command-arguments",
          boost::program_options::value<std::vector<std::string> >(),
         "positional parameter 1..N");

      parent_options_desc.add ( positional_options_desc );
    } 

    boost::program_options::store
    (
      boost::program_options::command_line_parser( argc, argv )
        .options ( parent_options_desc )
        .positional ( create_positional_options() )
        .run (),
      vm
    );

    boost::program_options::notify ( vm );

    process_parsed_options ( argc, argv, vm );

    process_parsed_positional_options ( argc, argv, vm );
  }

  /**
   * Processes positional options. By default dispatches the
   * subcommands. 
   */
  virtual void
  process_parsed_positional_options
  ( int argc, char** argv, const boost::program_options::variables_map& vm )
  const throw ( DoNotContinue )
  {
    if ( get_subcommands().size() > 0 )
    {
      for ( uint i = 0; i < get_subcommands().size(); ++i )
      {
        const generic_command& cmd = *get_subcommands()[i];

        if ( vm["command"].as<std::string>() == cmd.get_name() )
        {
          cmd.run ( argc, argv );

          return;
        }
      }  
    }
  }

  /**
   * The supplied variables_map contains correctly parsed
   * variables. You will probably want to redefine this method,
   * by default it only processes generic_command::create_options
   * options.
   *
   * @throw DoNotContinue If a switch parsed doesn't require
   *        further action (e.g.: --help, --version ). 
   */
  virtual void
  process_parsed_options
  ( int argc, char** arg, const boost::program_options::variables_map& vm )
  const throw ( DoNotContinue )
  {
    if ( vm.count("help") )
    {
      print_help();

      throw DoNotContinue();
    }
    else if ( vm.count("version") )
    {
      static const char* LICENSE =
      "Copyright 2011 Frederic Menou                                    \n"
      "                                                                 \n"
      "Magrit is free software: you can redistribute it and/or modify   \n"
      "it under the terms of the GNU Affero General Public License as   \n"
      "published by the Free Software Foundation, either version 3 of   \n"
      "the License, or (at your option) any later version.              \n"
      "                                                                 \n"
      "Magrit is distributed in the hope that it will be useful,        \n"
      "but WITHOUT ANY WARRANTY; without even the implied warranty of   \n"
      "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the    \n"
      "GNU Affero General Public License for more details.              \n"
      "                                                                 \n"
      "You should have received a copy of the GNU Affero General Public \n"
      "License along with Magrit.                                       \n"
      "If not, see <http://www.gnu.org/licenses/>.                      \n";

      std::cout << LICENSE << std::endl;
      std::cout << "Version 0.0.1" << std::endl;
      throw DoNotContinue();
    }
  }

  /**
   * Defines the command line options used by this command. Redefine
   * in the subclass to tailor to your need. Call this method in your
   * subclass if you want to have access to --help and --version switches.
   *
   * @return boost::program_options::options_description
   */
  virtual boost::program_options::options_description
  create_options () const 
  {

    boost::program_options::options_description
      generic_options_desc ( "Main options" );

    generic_options_desc.add_options()
      ("help,h", "produces this help message")
      ("version,v", "version of the application");

    return generic_options_desc;
  }

  /**
   * Subcommands implemented by the command. None by default.
   */
  virtual std::vector< sh_ptr<generic_command> > get_subcommands() const
  {
    return std::vector< sh_ptr<generic_command> >();
  }

  /**
   * Subcommand description. Same order as get_subcommands. None by default.
   */
  virtual std::vector<std::string> get_subcommands_desc() const
  {
    return std::vector<std::string>();
  }

  /**
   * Prints the help notice.
   */
  virtual void print_help () const
  {
    // Template method was not liking the "::" ?
    using namespace std;

    auto cmds      = get_subcommands();
    auto cmds_desc = get_subcommands_desc();

    cout << "Use: " << get_name() << " <options> ";

    join<string,vector<sh_ptr<generic_command> > >
    (
      " | ",
      cmds,
      ostream_iterator<string>( cout ),
      []( sh_ptr<generic_command> cmd ) -> string
      {
        return cmd->get_name(); 
      }
    );

    cout << ((cmds.size() > 0)? " <subcommand>":"") << endl << endl;

    cout << "For subcommand's arguments help, ";
    cout << "call the desired command with --help" << endl << endl;

    cout << ((cmds.size() > 0)? "Commands:":"") << endl;

    for (uint i = 0; i < cmds.size(); ++i )
    {
      cout << "  " << cmds[i]->get_name() << ":  " << cmds_desc[i] << endl;
    }

    cout << endl;
  
    cout <<  create_options() ;
  }

  /**
   * Defines the positional options. 
   *
   * @return boost::program_options::positional_options_description
   */ 
  boost::program_options::positional_options_description
  create_positional_options () const
  {
    boost::program_options::positional_options_description
      positional_options_desc;

    if ( get_subcommands().size() > 0 )
    {
      positional_options_desc
        .add("command",1)
        .add("command-arguments",-1);
    }

    return positional_options_desc;
  }
};

