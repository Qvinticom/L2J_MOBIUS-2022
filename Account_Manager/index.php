<?php
error_reporting(0);
include 'includes/config.php';
$error = "";
	if(isset($_POST['register'])) 
	{
		$conn = new mysqli($server_host, $db_user_name, $db_user_password, $db_database);
		// Check connection
		if (mysqli_connect_errno())
		{
			$error = "Can't Connect to MySQL <h5>". mysqli_connect_error()."</h5>";
			echo "Failed to connect to MySQL: " . mysqli_connect_error();
			exit();
		}
		  
		$account = mysqli_real_escape_string($conn, $_POST['username']);
		$password = base64_encode(sha1($_POST['password'], true));
		$email = $_POST['email'];
		
		if($_POST['password']!=$_POST['passwordVerify']){
			$error .= "Password does not match.<br>"; 
		}
		
		if(mb_strlen($account)<4 || mb_strlen($account)>14){
			$error .= "Account length must be 4 to 14 characters long.";
			}
			
		if(mb_strlen($_POST['password'])<4 || mb_strlen($_POST['password'])>16){
			$error .= "Password length must be 4 to 16 characters long.";
			}
			
		if(mb_strlen($email)<7 || mb_strlen($email)>100){
			$error .= "Email length must be 7 to 100 characters long.";
			}

		$sql = "SELECT `login` FROM `accounts` WHERE `login`='".$account."'";
		$result = $conn->query($sql);
		if ($result->num_rows!=0) {
			$error .= "Account already exists.<br>";	
		}		
		
		if(empty($error)){
			echo ($account.$password.$email);
			$sqlregister = "INSERT INTO `accounts` (`login`, `password`, `email`, `lastIP`) VALUES ('".$account."','".$password."','".$email."','".$_SERVER['REMOTE_ADDR']."')";
			if ($conn->query($sqlregister) === TRUE) {
				$error = "Account created!";
				header( "refresh:5;url=index.php" );
			} else {
				$error = "Something went wrong.";
			}
		}
		
		$conn->close();
	}
	
	if(isset($_POST['login'])) 
	{
		$conn = new mysqli($server_host, $db_user_name, $db_user_password, $db_database);
		// Check connection
		if (mysqli_connect_errno())
		{
			$error = "Can't Connect to MySQL <h5>". mysqli_connect_error()."</h5>";
			echo "Failed to connect to MySQL: " . mysqli_connect_error();
			exit();
		}
		  
		$account = mysqli_real_escape_string($conn, $_POST['username']);
		$password = base64_encode(sha1($_POST['password'], true));
		
		if($account == '')
			$error = 'Enter account';
		if($password == '')
			$error = 'Enter password';
		
		$sql = "SELECT * FROM `accounts` WHERE `login`='".$account."'";
		$result = $conn->query($sql);

			if ($result->num_rows > 0) {
				while($row = $result->fetch_assoc()) 
				{
					
					if ($password == $row['password']) 
						{
							$_SESSION['account'] = $account;
							$_SESSION['password'] = $password;
							$error = "You are connected. Redirecting . . .";
							header( "refresh:1;url=dashboard.php" );	
						}
					else
						{
							$error = 'Password does not match.';
						}
				}
			} 
			else 
			{
				$error = 'Account does not exist. <a data-target="#modalRegister" data-toggle="modal" type="button">Create one.</a>';
			}
		
		$conn->close();
	}
	if(isset($_POST['forgot'])) 
	{
		$conn = new mysqli($server_host, $db_user_name, $db_user_password, $db_database);
		// Check connection
		if (mysqli_connect_errno())
		{
			$error = "Can't Connect to MySQL <h5>". mysqli_connect_error()."</h5>";
			echo "Failed to connect to MySQL: " . mysqli_connect_error();
			exit();
		}
		  
		$account = mysqli_real_escape_string($conn, $_POST['username']);
		$email = $_POST['email'];
		$admin = $CONFIG['emailaddress'];
		//get a random password
		$password_rnd = rand(9999, 999999);
		//encode password
		$password = base64_encode(sha1($password_rnd, true));
		
		if($account == '')
			$error = 'Enter account';
		if($email == '')
			$error = 'Enter email';
		
		$sql = "SELECT * FROM `accounts` WHERE `login`='".$account."' AND `email`='".$email."'";
		$result = $conn->query($sql);

			if ($result->num_rows > 0) {
				while($row = $result->fetch_assoc()) 
				{
					
					if ($email == $row['email']) 
						{
							if ($account == $row['login']) 
								{
									$to = $email;
									$subject = 'Your recovered Password';
									$message = 'Use this password to login '. $password_rnd;
									$headers = 'From:'. $admin;
									if (mail($to, $subject, $message, $headers)){
										$update = "UPDATE `accounts` SET `password`='".$password."' WHERE `login`='".$account."'";
										$resultupdate = $conn->query($update);
										if ($resultupdate)
										{
											$error = 'Your password has been sent to your email';
										}
										else 
										{
											$error = 'Fail to recover your password';
										}
									}
									else
									{
										$error = 'Failed - Contact Administrator '.$admin;
									}
								}
							else {
								$error = 'Account does not match.';
							}
							
						}
					else
						{
							$error = 'Email does not match.';
						}
				}
			} 
			else 
			{
				$error = 'Email or Account does not match.';
			}
		
		$conn->close();
	}
?>

<!DOCTYPE html>

<html lang="en">
<head>
	<meta content="text/html; charset=utf-8" http-equiv="content-type">
	<meta charset="utf-8">
	<meta content="IE=edge" http-equiv="X-UA-Compatible">
	<meta content="" name="description">
	<meta content="" name="keywords">
	<script src="js/jquery.min.js" type="text/javascript">
	</script>

	<title>Mobius Underground - Main</title>
	<link href="css/style.css" rel="stylesheet">
	<link href="images/loader.png" rel="icon" type="image/png">
	<script src='https://www.google.com/recaptcha/api.js'>
	</script>
	<link href='https://fonts.googleapis.com/css?family=Lato:300,400,500' rel='stylesheet' type='text/css'>
	<script src="js/bootstrap.min.js">
	</script>
	<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">
	<script language="javascript">
	                   $(document).ready(function(){

	                    $('#register').submit(function() {
	                      
	                    if($('#password').val() != $('#passwordVerify').val()){
	                       alert("Please re-enter confirm password");
	                       $('#passwordVerify').val('');
	                       $('#passwordVerify').focus();
	                       return false;
	                    }

	                        function clear_form()
	                        {
	                           $("#email").val('');
	                           $("#username").val('');
	                           $("#password").val('');
	                           $("#passwordVerify").val('');
	                        }
	                   });
	                });
	</script>
</head>

<body>
	<div id="preloader">
		<div id="preloader-image">
		</div>
	</div>


	<div class="p-anim">
	</div>
	<video playsinline autoplay muted loop id="bgvid">
		<source src="media/bg.webm" type="video/webm">
		<source src="media/bg.mp4" type="video/mp4">
	</video>

	<header>
		<div class="container">
			<div class="header-left">
			</div>


			<div class="header-right">
				<div class="logo">
				</div>


				<div class="menu">
					<a href="index.php">MAIN</a> <a href="download.php">DOWNLOAD</a> <a href="#">DONATE</a> <a href="<?php echo $forum;?>">FORUM</a>
				</div>
				<br>
				<img src="images/logo.png" width="100%">

				<div class="statuses">
					<div class="entercp">
						Login to your <a data-target="#modalLogin" data-toggle="modal" type="button">Account</a> or 
						<a data-target="#modalForgot" data-toggle="modal" type="button">RESTORE PASSWORD</a>
					</div>
					<br>


					<div class="register">
						<a data-target="#modalRegister" data-toggle="modal" type="button">CREATE AN ACCOUNT</a>
					</div>
					<div class="messages">
							<h4><font color="#FFFFFF"><?php
								echo (!empty($error)?"<label><strong>".$error."</strong></label>":'');
								?></font>
							</h4>
						</div>
				</div>
			</div>
		</div>
	</header>


	<div class="modal fade" id="modalRegister" role="dialog">
		<div class="container">
			<br>
			<!-- Modal content-->


			<div class="form">
				<div class="modal-body">
					<div class="modal-header">
						<button class="close" data-dismiss="modal" type="button">&times;</button>

						<h4 class="modal-title">Register Account</h4>
					</div>


					<div>
						<form id="register" method="post">
							<div class="form-group">
								<input class="form-control" data-error="Account name is required." id="username" name="username" placeholder="Please enter your Account" required="required" type="text" value="<?php if(isset($_POST['username'])) echo $_POST['username'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>


							<div class="form-group">
								<input class="form-control" data-error="Valid email is required." id="email" name="email" placeholder="Please enter your Email" required="required" type="email" value="<?php if(isset($_POST['email'])) echo $_POST['email'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>


							<div class="form-group">
								<input class="form-control" data-error="Password is required." id="password" name="password" placeholder="Please enter your Password" required="required" type="password" value="<?php if(isset($_POST['password'])) echo $_POST['password'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>


							<div class="form-group">
								<input class="form-control" data-error="Verify Password is required." id="passwordVerify" name="passwordVerify" placeholder="Please re-enter your Password" required="required" type="password" value="<?php if(isset($_POST['password'])) echo $_POST['password'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>
							<input class="form-btn btn" id="submit" name="register" type="submit" value="REGISTER">
						</form>
						
					</div>


					<div class="modal-footer">
						<div class="messages">
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<div class="modal fade" id="modalLogin" role="dialog">
		<div class="container">
			<br>
			<!-- Modal content-->


			<div class="form">
				<div class="modal-body">
					<div class="modal-header">
						<button class="close" data-dismiss="modal" type="button">&times;</button>

						<h4 class="modal-title">Login</h4>
					</div>


					<div>
						<form id="login" method="post">
							<div class="form-group">
								<input class="form-control" data-error="Account name is required." id="username" name="username" placeholder="Please enter your Account" required="required" type="text" value="<?php if(isset($_POST['username'])) echo $_POST['username'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>


							<div class="form-group">
								<input class="form-control" data-error="Password is required." id="password" name="password" placeholder="Please enter your Password" required="required" type="password" value="<?php if(isset($_POST['password'])) echo $_POST['password'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>

							<input class="form-btn btn" id="submit" name="login" type="submit" value="LOGIN">
							
						</form>
					</div>


					<div class="modal-footer">
						<div class="messages">
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<div class="modal fade" id="modalForgot" role="dialog">
		<div class="container">
			<br>
			<!-- Modal content-->


			<div class="form">
				<div class="modal-body">
					<div class="modal-header">
						<button class="close" data-dismiss="modal" type="button">&times;</button>

						<h4 class="modal-title">Forgot My password</h4>
					</div>


					<div>
						<form id="login" method="post">
							<div class="form-group">
								<input class="form-control" data-error="Account name is required." id="username" name="username" placeholder="Please enter your Account" required="required" type="text" value="<?php if(isset($_POST['username'])) echo $_POST['username'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>
							
							<div class="form-group">
								<input class="form-control" data-error="Valid email is required." id="email" name="email" placeholder="Please enter your Email" required="required" type="email" value="<?php if(isset($_POST['email'])) echo $_POST['email'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>

							<input class="form-btn btn" id="submit" name="forgot" type="submit" value="Restore">
							
						</form>
					</div>


					<div class="modal-footer">
						<div class="messages">
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>


	<div class="footer">
		<a href="http://l2jmobius.org"><img alt="" src="images/l2jmobius.png" title=""></a>
	</div>
	<script>
	               var url = 'index.php';
	</script> 
	<script src="js/jquery.cookie.min.js">
	</script> 
	<script src="js/scripts.js">
	</script> 
	<script src="js/validator.js">
	</script> 
	<script src="js/register.js">
	</script>
<!--[if lt IE 9]>
<script src="//oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
<script src="//oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
<![endif]-->
</body>
</html>
