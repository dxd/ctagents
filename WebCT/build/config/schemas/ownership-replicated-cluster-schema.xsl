<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" indent="yes"/>
	<xsl:template match="/">
		<cluster-config>
			<xsl:copy-of select="cluster-config/cluster-name"/>
			<xsl:copy-of select="cluster-config/dist-cache"/>
			<xsl:copy-of select="cluster-config/jms"/>
			<description>To ensure data coherency for update operations you may use the ownership model. With this approach each space is responsible for performing updates on different segments of the data i.e. there is no option to perform update operations on two copies of the same object located in different spaces at the same time updates are always routed to the space that owns the Entry.</description>
			<notify-recovery>true</notify-recovery>
			<cache-loader>
				<external-data-source>${com.gs.cluster.cache-loader.external-data-source}</external-data-source>
				<central-data-source>${com.gs.cluster.cache-loader.central-data-source}</central-data-source>
			</cache-loader>
			<mirror-service>
	            <enabled>false</enabled>
	            <url>jini://*/mirror-service_container/mirror-service</url>
	            <bulk-size>100</bulk-size>
	            <interval-millis>2000</interval-millis>
	            <interval-opers>100</interval-opers>
	        </mirror-service>
			<cluster-members>
				<xsl:copy-of select="cluster-config/cluster-members/member"/>
			</cluster-members>
			<groups>
				<group>
					<group-name>ownership_replicated_group</group-name>
					<group-members>
						<xsl:copy-of select="cluster-config/cluster-members/member"/>
					</group-members>
					<load-bal-policy>
						<apply-ownership>false</apply-ownership>
						<disable-parallel-scattering>false</disable-parallel-scattering>
						<proxy-broadcast-threadpool-min-size>4</proxy-broadcast-threadpool-min-size>
						<proxy-broadcast-threadpool-max-size>64</proxy-broadcast-threadpool-max-size>
						<write>
							<policy-type>hash-based</policy-type>
							<broadcast-condition>never</broadcast-condition>
						</write>
						<read>
							<policy-type>local-space</policy-type>
							<broadcast-condition>routing-index-is-null</broadcast-condition>
						</read>
						<take>
							<policy-type>hash-based</policy-type>
							<broadcast-condition>routing-index-is-null</broadcast-condition>
						</take>
						<notify>
							<policy-type>local-space</policy-type>
							<broadcast-condition>never</broadcast-condition>
						</notify>
						<default>
							<policy-type>hash-based</policy-type>
							<broadcast-condition>routing-index-is-null</broadcast-condition>
						</default>
					</load-bal-policy>
				<repl-policy>
					<replication-mode>async</replication-mode>
					<permitted-operations>write, take, extend_lease, update, discard, lease_expiration, notify</permitted-operations>
					<policy-type>partial-replication</policy-type>
					<recovery>true</recovery>
					<replicate-notify-templates>false</replicate-notify-templates>
					<trigger-notify-templates>true</trigger-notify-templates>
					<repl-find-timeout>5000</repl-find-timeout>
					<repl-find-report-interval>30000</repl-find-report-interval>
					<repl-original-state>true</repl-original-state>
					<communication-mode>unicast</communication-mode>
					<redo-log-capacity>-1</redo-log-capacity>
					<redo-log-memory-capacity>-1</redo-log-memory-capacity>
					<recovery-chunk-size>200</recovery-chunk-size>
					<recovery-thread-pool-size>4</recovery-thread-pool-size>
					<async-replication>
						<sync-on-commit>false</sync-on-commit>
						<sync-on-commit-timeout>300000</sync-on-commit-timeout>
						<repl-chunk-size>500</repl-chunk-size>
						<repl-interval-millis>3000</repl-interval-millis>
						<repl-interval-opers>500</repl-interval-opers>
                        <reliable>false</reliable>
                        <async-channel-shutdown-timeout>300000</async-channel-shutdown-timeout>
                    </async-replication>
					<sync-replication>
						<todo-queue-timeout>1500</todo-queue-timeout>
						 <hold-txn-lock>false</hold-txn-lock>
						<unicast>
							<min-work-threads>4</min-work-threads>
							<max-work-threads>16</max-work-threads>
						</unicast>
						<multicast>
							<adaptive>true</adaptive>
							<ip-group>224.0.0.1</ip-group>
							<port>28672</port>
							<ttl>4</ttl>
							<min-work-threads>4</min-work-threads>
							<max-work-threads>16</max-work-threads>
						</multicast>
					</sync-replication>
				</repl-policy>
				</group>
			</groups>
		</cluster-config>
	</xsl:template>
</xsl:stylesheet>
